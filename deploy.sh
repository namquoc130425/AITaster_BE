#!/bin/bash

set -e

SERVER=root@203.145.47.211
APP_DIR=/var/www/be
JAR_NAME=be.jar
PORT=8080

 echo "Building app..."
 ./mvnw clean package -DskipTests

echo "Deploy files to server..."
scp target/${JAR_NAME} ${SERVER}:${APP_DIR}/${JAR_NAME}

echo "Restart server..."
ssh ${SERVER} << EOF
set -e

APP_DIR=${APP_DIR}
JAR_NAME=${JAR_NAME}
PORT=${PORT}

cd \$APP_DIR

echo "Checking old app on port \$PORT..."
pid=\$(sudo lsof -t -i:\$PORT || true)

if [ -n "\$pid" ]; then
    echo "Killing old app on port \$PORT, PID: \$pid"
    sudo kill -9 \$pid || true
else
    echo "No app running on port \$PORT"
fi

echo "Checking old Java process for \$JAR_NAME..."
old_java_pid=\$(pgrep -f "\$JAR_NAME" || true)

if [ -n "\$old_java_pid" ]; then
    echo "Killing old Java process: \$old_java_pid"
    sudo kill -9 \$old_java_pid || true
fi

echo "Loading .env..."
if [ -f .env ]; then
    set -a
    source .env
    set +a
else
    echo "ERROR: .env not found in \$APP_DIR"
    exit 1
fi

echo "Force server port = \$PORT"
export SERVER_PORT=\$PORT

echo "Starting server..."
rm -f app.log
nohup java -jar \$JAR_NAME > app.log 2>&1 &

echo "Waiting for app to open port \$PORT..."

for i in {1..30}; do
    new_pid=\$(sudo lsof -t -i:\$PORT || true)

    if [ -n "\$new_pid" ]; then
        echo "Server started successfully on port \$PORT. PID: \$new_pid"
        exit 0
    fi

    java_pid=\$(pgrep -f "\$JAR_NAME" || true)

    if [ -z "\$java_pid" ]; then
        echo "Java process stopped. Last logs:"
        tail -n 120 app.log
        exit 1
    fi

    echo "Still starting... \$i/30"
    sleep 2
done

echo "Server failed to open port \$PORT after waiting. Last logs:"
tail -n 150 app.log
exit 1
EOF

echo "Done!"