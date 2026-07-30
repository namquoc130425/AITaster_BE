package com.example.AiTaster.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowEmailTemplateTest {

    private final TemplateEngine templateEngine = templateEngine();

    @ParameterizedTest(name = "{0}")
    @MethodSource("workflowTemplates")
    void workflowTemplate_rendersVietnameseContentAndBothParticipantNames(
            String templateName,
            String titleVariable,
            String vietnameseMessage
    ) {
        Context context = new Context();
        context.setVariable("clientName", "Nguyễn Minh Anh");
        context.setVariable("expertName", "Trần Quốc Bảo");
        context.setVariable(titleVariable, "Chatbot chăm sóc khách hàng");

        String html = templateEngine.process(templateName, context);

        assertThat(html)
                .contains("Nguyễn Minh Anh")
                .contains("Trần Quốc Bảo")
                .contains("Chatbot chăm sóc khách hàng")
                .contains(vietnameseMessage);
    }

    private static Stream<Arguments> workflowTemplates() {
        return Stream.of(
                Arguments.of(
                        "workflow-expert-applied",
                        "jobTitle",
                        "vừa gửi hồ sơ ứng tuyển"
                ),
                Arguments.of(
                        "workflow-invitation-received",
                        "projectTitle",
                        "đã gửi cho bạn lời mời"
                ),
                Arguments.of(
                        "workflow-invitation-accepted",
                        "projectTitle",
                        "đã chấp nhận lời mời"
                ),
                Arguments.of(
                        "workflow-project-started",
                        "projectTitle",
                        "đã hoàn tất thanh toán ký quỹ"
                )
        );
    }

    private TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
