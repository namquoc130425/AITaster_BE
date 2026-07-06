# Báo cáo đánh giá code nhánh release/v0.6

Ngày cập nhật: 2026-07-03

Mục tiêu: đánh giá code hiện tại, chỉ ra rủi ro nghiệp vụ/logic, và đề xuất cách khắc phục dễ hiểu. Tài liệu này không sửa source code.

## 1. Trạng thái kiểm thử

Mình đã thử chạy toàn bộ test bằng Maven nhưng môi trường hiện tại chặn việc tạo tiến trình Maven. Vì vậy chưa lấy được kết quả test thật.

Hiện repo chỉ có một test khởi động app là ApplicationTests.contextLoads. Test này chưa kiểm tra các luồng nghiệp vụ như JobPost AI, AI Service, Proposal, Invitation, thanh toán, nạp tiền, rút tiền.

Nên bổ sung test cho các luồng chính trước khi release.

## 2. Kết luận nhanh

Các điểm cần ưu tiên:

1. Endpoint tạo admin đang public. Đây là lỗi bảo mật nghiêm trọng.
2. API xem ví theo walletId có thể làm lộ thông tin ví người khác.
3. JobPost AI tạo bản nháp là hợp lý nếu có nút Save riêng. Đây không phải lỗi, nhưng API/FE cần thể hiện rõ bản nháp chưa lưu.
4. Rút tiền chưa đóng băng số dư khi user gửi yêu cầu, dễ lệch tiền thật.
5. Webhook thanh toán đã có xử lý cơ bản, nhưng cần chắc hơn khi mạng lag hoặc provider gửi lại webhook.
6. Một số luồng mua hàng/mở khóa thiếu chống double click hoặc request song song.
7. File cấu hình đang chứa thông tin nhạy cảm như password DB và JWT key.

## 3. JobPost AI

### Đánh giá

Sau khi bạn giải thích, luồng này là hợp lý:

1. Client nhập yêu cầu.
2. AI tạo bản nháp.
3. User xem và chỉnh bản nháp.
4. User bấm Save thì mới lưu JobPost thật.

Vì vậy việc JobPost AI chưa save DB không phải lỗi nếu thiết kế sản phẩm là như trên.

### Rủi ro còn lại

- Frontend hoặc user có thể hiểu nhầm rằng bản nháp AI đã được lưu.
- Controller đang gom mọi lỗi thành lỗi 400, kể cả lỗi AI, lỗi Qdrant, lỗi server.
- AI được dặn giữ nguyên budget và timeline, nhưng code chưa kiểm tra lại sau khi AI trả kết quả.

### Hướng khắc phục

- Giữ luồng hiện tại: AI chỉ tạo bản nháp preview.
- Response nên có thông tin rõ: draftOnly = true hoặc message là Bản nháp chưa được lưu, hãy bấm Save để lưu.
- Khi bấm Save, frontend gửi nội dung bản nháp sang API tạo JobPost thật.
- Sau khi AI trả về, backend nên kiểm tra:
  - Budget phải bằng budget user nhập, hoặc dùng lại budget gốc.
  - Timeline phải bằng timeline user nhập, hoặc dùng lại timeline gốc.
  - Skill AI chọn phải nằm trong danh sách skill Qdrant trả về.
  - Số skill tối đa nên là 5.
- Không nên bắt tất cả exception rồi trả 400. Nên phân biệt:
  - Lỗi user nhập sai: 400.
  - Lỗi AI/Qdrant: 502 hoặc 503.
  - Lỗi server: 500.

### Test cần có

- AI tạo bản nháp nhưng chưa lưu DB.
- Bấm Save mới tạo JobPost thật.
- AI trả skill ngoài danh sách thì bị loại.
- AI trả sai budget hoặc timeline thì hệ thống xử lý đúng.
- Qdrant không tìm thấy skill thì báo lỗi dễ hiểu.

## 4. JobPost thường

### Rủi ro

Comment trong code nói chỉ update khi JobPost còn Draft, nhưng service hiện chưa thấy chặn update khi JobPost đã Open, Closed hoặc Hidden.

Nếu cho sửa job đã Open hoặc Closed thì có thể gây lệch nghiệp vụ:

- Expert đã apply theo yêu cầu cũ.
- Client sửa yêu cầu mới sau khi đã có proposal.
- Invitation hoặc payment sau đó không còn khớp với yêu cầu ban đầu.

### Hướng khắc phục

- Chỉ cho update JobPost khi status là Draft.
- Nếu JobPost đã Open, nên yêu cầu client đóng job rồi tạo job mới.
- Nếu JobPost đã có application hoặc invitation, không nên xóa cứng. Nên dùng Hidden hoặc soft delete.

### Test cần có

- Owner sửa Draft thành công.
- Owner sửa Open hoặc Closed bị chặn.
- User khác sửa job bị chặn.
- Tạo trùng JobPost active bị chặn.

## 5. AI Service / Expert Service

### Rủi ro

- Chưa thấy chặn client mua AI service của chính mình.
- Mua service bằng ví có thể bị double click và trừ tiền nhiều lần.
- Update service chưa thấy xử lý rõ file mới và file cũ.
- API đổi trạng thái service nhận trực tiếp enum, nên cần quy định trạng thái nào được phép chuyển.

### Hướng khắc phục

Chống mua trùng:

- Tạo bảng ServicePurchase hoặc Order để ghi nhận client nào đã mua service nào.
- Nếu một service chỉ cần mua một lần, thêm ràng buộc không cho cùng client mua lại cùng service.
- Backend cần có idempotency key hoặc unique constraint, không chỉ dựa vào việc frontend disable nút.

Xử lý file khi update:

- Nếu request có file mới, lưu file mới và đánh dấu file cũ không active.
- Nếu request không có file mới, giữ file cũ.

Quản lý status:

- Chỉ cho chuyển trạng thái hợp lệ, ví dụ Open sang Closed, Closed sang Open, Open hoặc Closed sang Deleted.
- Không cho Deleted quay lại Open nếu business không cho khôi phục.

### Test cần có

- Client không mua service của chính mình.
- Double click mua service không bị trừ tiền hai lần.
- Service Deleted hoặc Closed không mua được.
- Update service có file mới thì file mới được dùng.

## 6. Proposal và mở khóa proposal

### Rủi ro

Code có kiểm tra đã mở khóa chưa, đây là đúng hướng. Nhưng nếu hai request đến cùng lúc, cả hai có thể cùng kiểm tra thấy chưa mở khóa rồi cùng trừ tiền.

### Hướng khắc phục

- Thêm ràng buộc ở database: một client chỉ có một unlock active cho một proposal.
- Khi mua bằng ví, lock proposal hoặc lock cặp proposal-client trước khi trừ tiền.
- Với SePay, vẫn cần unique constraint để webhook gửi lại không tạo unlock lần hai.

### Test cần có

- Mở khóa proposal thành công bằng ví.
- Mở khóa proposal lần hai bị chặn.
- Hai request song song chỉ một request thành công.
- Webhook SePay gửi lại không tạo unlock lần hai.

## 7. Invitation

### Điểm tốt

- Có kiểm tra invitation hết hạn.
- Có kiểm tra client là chủ JobPost.
- Có kiểm tra expert đúng người được mời.
- Có chặn tạo invitation mới khi đang có invitation pending còn hạn.

### Rủi ro

Khi expert accept invitation, cần chắc chắn JobPost chưa có invitation accepted khác. Hiện logic check accepted nằm ở lúc tạo invitation. Ở thời điểm accept vẫn nên kiểm tra lại trong transaction.

Nếu có request song song, có thể xảy ra trường hợp hai invitation cùng được accept cho một JobPost.

### Hướng khắc phục

- Khi accept invitation, lock JobPost hoặc lock các invitation của JobPost trong transaction.
- Trước khi set Accepted, kiểm tra lại không có invitation Accepted khác.
- Thêm rule ở service hoặc database: mỗi JobPost chỉ có tối đa một invitation Accepted.
- Việc expire invitation nên chạy bằng scheduled job hoặc batch, không nên quét toàn bộ mỗi lần user gọi API.

### Test cần có

- Expert accept đúng invitation thành công.
- Expert khác accept bị chặn.
- Invitation hết hạn không accept được.
- Hai invitation accept cùng lúc chỉ một cái thành công.

## 8. Thanh toán SePay và mạng lag

### Điểm tốt

- Có kiểm tra secret key webhook.
- Có kiểm tra trạng thái thanh toán từ SePay.
- Có kiểm tra tiền tệ VND.
- Có kiểm tra số tiền thanh toán khớp payment pending.
- Có lock payment theo paymentCode khi xử lý.
- Có tái sử dụng pending payment nếu user bấm tạo lại thanh toán khi payment cũ còn hạn và số tiền không đổi.

### Rủi ro

- Provider transaction code chưa thấy unique constraint ở database. Nếu SePay gửi cùng webhook hai lần rất nhanh, cả hai request có thể cùng qua bước kiểm tra trùng.
- Nếu user thanh toán sau khi payment hết hạn, code hiện đánh dấu Expired và không xử lý tiền. Nhưng tiền thật có thể đã vào ngân hàng, cần đối soát.
- Nếu sai số tiền, code đánh dấu Failed. Cần lưu lại để admin xử lý tiền thừa hoặc thiếu.
- Nếu thiếu payment code, service return im lặng, vận hành sẽ khó biết provider gửi sai format.

### Hướng khắc phục

Idempotency:

- Thêm unique index cho paymentCode.
- Thêm unique index cho providerTransactionCode khi không null.
- Chỉ cho chuyển payment từ Pending sang Success một lần.

Đối soát:

- Tạo bảng PaymentWebhookLog để lưu raw body, provider transaction code, payment code, trạng thái xử lý.
- Với thanh toán sau hết hạn, nên đưa vào trạng thái Needs Review thay vì bỏ qua.
- Với sai số tiền, nên lưu trạng thái Amount Mismatch để admin xử lý.

### Test cần có

- SePay gửi lại webhook giống nhau hai lần.
- Hai webhook chạy song song.
- Sai số tiền.
- Thanh toán sau khi expired.
- Thiếu invoice number nhưng description có payment code.

## 9. Nạp tiền ví

### Điểm tốt

- Có kiểm tra user là chủ ví.
- Có kiểm tra ví Active.
- Có kiểm tra currency là VND.
- Có giới hạn số tiền nạp tối thiểu và tối đa.
- Có chặn số tiền thập phân.

### Rủi ro

- Nếu ví bị khóa sau khi tạo pending payment nhưng trước khi webhook về, code đánh dấu payment failed. Tuy nhiên tiền thật có thể đã chuyển, cần đối soát.
- Handler đang dùng RuntimeException khi không tìm thấy ví, nên lỗi chưa thống nhất với hệ thống.
- Vẫn cần unique/idempotency cho webhook như mục thanh toán SePay.

### Hướng khắc phục

- Nếu ví không thể cộng tiền, chuyển payment sang trạng thái cần admin kiểm tra thay vì chỉ Failed.
- Ghi log webhook đầy đủ để đối soát.
- Không dùng RuntimeException trực tiếp, dùng exception chuẩn của dự án hoặc mark payment cần review.

### Test cần có

- Nạp dưới min bị chặn.
- Nạp trên max bị chặn.
- Nạp số lẻ bị chặn.
- Webhook trùng không cộng tiền hai lần.
- Ví inactive sau khi tạo payment thì chuyển sang trạng thái cần review.

## 10. Rút tiền ví

### Rủi ro lớn nhất

Khi user gửi yêu cầu rút tiền, hệ thống chỉ lưu requestWithdrawal và amountRequestWithdrawal. Tiền vẫn nằm trong balance và có thể tiếp tục được dùng.

Tình huống lỗi dễ xảy ra:

1. User có 1,000,000 VND.
2. User yêu cầu rút 1,000,000 VND.
3. Admin chuyển khoản ngân hàng thủ công.
4. Trước khi admin approve trong hệ thống, user dùng ví mua service 500,000 VND.
5. Admin approve thì hệ thống không đủ tiền để trừ, hoặc dữ liệu tiền bị lệch với tiền thật đã chuyển.

### Hướng khắc phục

Nên dùng cơ chế đóng băng tiền:

- Khi user request withdraw: lock ví, trừ balance, cộng frozenBalance.
- Khi admin reject: trừ frozenBalance, cộng lại balance.
- Khi admin approve: trừ frozenBalance, tạo transaction rút tiền thành công.

Nên tạo entity riêng WithdrawalRequest thay vì chỉ lưu boolean trên ví:

- Pending: đang chờ admin xử lý.
- Approved: admin đã duyệt và hệ thống đã trừ frozen balance.
- Rejected: admin từ chối và tiền đã trả lại balance.
- Lưu thêm bank account snapshot, số tiền, người duyệt, thời gian duyệt, ghi chú.

### Test cần có

- Request withdraw làm balance giảm và frozenBalance tăng.
- Reject trả tiền về balance.
- Approve trừ frozenBalance.
- Double approve không trừ hai lần.
- User không thể tiêu phần tiền đang frozen.

## 11. Hàm tính phí và chuyển tiền nội bộ

### Rủi ro

Hàm calculateFee nghe như chỉ tính phí, nhưng thực tế vừa tính phí vừa tạo transaction cộng tiền cho admin. Tên hàm dễ gây hiểu nhầm. Nếu sau này ai gọi hàm này chỉ để preview phí, hệ thống có thể cộng tiền admin sai.

### Hướng khắc phục

- Tách thành hai hàm:
  - calculatePlatformFee: chỉ tính số tiền phí, không tạo transaction.
  - collectPlatformFee: tạo transaction thu phí thật.
- Hoặc đổi tên rõ hơn: collectPlatformFeeAndReturnNetAmount.
- Với mọi luồng tiền, nên kiểm tra tổng tiền: gross amount bằng tiền expert nhận cộng phí sàn.

### Test cần có

- Mua service 100,000 với phí 10 phần trăm thì expert nhận 90,000 và admin nhận 10,000.
- Nếu ví client không đủ tiền thì không ai được cộng tiền.
- Nếu cộng tiền expert lỗi thì phí admin cũng phải rollback.

## 12. Ưu tiên khắc phục

### P0 - Nên sửa ngay

1. Khóa endpoint tạo admin.
2. Chặn user xem ví người khác bằng walletId.
3. Đưa password, JWT key, Redis password ra biến môi trường.
4. Rotate secret nếu các secret này đã từng được push lên remote.

### P1 - Sửa cho luồng chính ổn định

1. Làm rõ contract JobPost AI draft: preview trước, Save mới lưu.
2. Thêm đóng băng tiền khi rút ví.
3. Thêm idempotency/unique constraint cho webhook/payment.
4. Chống double purchase AI service và double unlock proposal.
5. Chống accept hai invitation cho cùng một JobPost.

### P2 - Tăng khả năng vận hành

1. Thêm bảng log webhook để đối soát.
2. Thêm trạng thái Needs Review cho payment bất thường.
3. Tách hàm tính phí khỏi hàm thu phí thật.
4. Chuyển expire invitation sang scheduled job.
5. Viết test nghiệp vụ cho các luồng tiền.

## 13. Ghi chú worktree

Trước khi cập nhật tài liệu, git status đã có một số file trong uploads/service-files bị đánh dấu deleted. Tài liệu này không sửa, không khôi phục, và không đụng vào các file đó.
