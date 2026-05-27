[AITasker_Backend_Engineering_Management_Document_V3_FULL_VI.md](https://github.com/user-attachments/files/28305847/AITasker_Backend_Engineering_Management_Document_V3_FULL_VI.md)
# TÀI LIỆU QUẢN LÝ TRIỂN KHAI BACKEND AITasker 

**Dự án:** AITasker — AI Marketplace Platform for AI Automation Services  

**Loại tài liệu:** Tài liệu quản lý triển khai backend / Kế hoạch sprint / Quy tắc quản trị kiến trúc  

**Timeline:** Week 3 → Week 10  

**Đội backend:** 3 Backend Developers  

**Mục tiêu:** Week 7 backend core gần hoàn thiện, Week 8 gần như hoàn thiện toàn bộ backend, Week 9 chỉ integration/testing/security fixing/bug fixing, Week 10 demo/deployment preparation/defense preparation.

---

## Quy tắc đọc tài liệu

- Tài liệu này dùng để quản lý sprint thật, không phải outline demo.
- Mọi task phải đi theo SRS, ERD, activity flow và state transition.
- Tên entity, enum, API, package, branch git được giữ tiếng Anh để khớp code.
- Mọi giải thích, mục tiêu, rủi ro, checklist và task được viết bằng tiếng Việt.
- Không developer nào được tự ý đơn giản hóa flow payment, escrow, wallet, review, dispute hoặc notification.

## Khóa nghiệp vụ bắt buộc

| Khóa nghiệp vụ | Quy tắc backend bắt buộc | Tác động |
| --- | --- | --- |
| Custom Project Flow | Project chỉ được tạo sau khi Expert accept ProjectInvitation. | Không tạo Project khi Client mới chọn Expert hoặc mới gửi invitation. |
| Escrow Activation | Project chỉ ACTIVE khi ProjectEscrow.status = HELD và amountPaid >= agreedAmount. | Chặn Expert submit Deliverable trước khi Client fund escrow. |
| Ready-made Service Flow | ExpertService purchase dùng ServiceOrder + ServiceDelivery + ClientService; không dùng ProjectEscrow. | Không trộn service order với custom project. |
| Money Movement | Mọi thay đổi tiền phải đi qua PaymentTransaction + Wallet/ProjectEscrow trong cùng transaction. | Chống double payment, lệch ví, mất audit trail. |
| Review Permission | Review chỉ được tạo sau Project COMPLETED hoặc ServiceOrder COMPLETED. | Không cho review giả hoặc review trước khi giao hàng. |
| Ownership Validation | Role check chưa đủ; phải check owner/participant của resource. | Chặn Client/Expert xem hoặc sửa dữ liệu không thuộc mình. |
| Notification Trigger | Business event quan trọng phải tạo Notification. | Frontend luôn có trạng thái để hiển thị cho user. |


# 1. CHIẾN LƯỢC TRIỂN KHAI BACKEND

## 1.1 Chiến lược delivery

Backend phải triển khai theo dependency chain. Không chia việc theo kiểu mỗi người làm rời rạc rồi ghép sau. Với AITasker, rủi ro lớn nằm ở lifecycle, escrow, wallet, permission và transaction consistency. Vì vậy thứ tự triển khai phải đi từ nền tảng bảo mật, đến pre-project flow, đến project/escrow, rồi mới tới deliverable/release/payment/reporting.

| Giai đoạn | Mục tiêu kỹ thuật | Không được bỏ qua |
| --- | --- | --- |
| Week 3 | Khóa security foundation, APIResponse, exception, validation, User/Profile, Wallet baseline. | JWT, RBAC, ownership helper, error contract, base response. |
| Week 4 | Hoàn thiện pre-project flow: JobPost, Application/Proposal, Invitation. | Không tạo Project ở tuần này nếu Expert chưa accept invitation. |
| Week 5 | Tạo Project transaction và ProjectEscrow funding. | acceptInvitation và fundEscrow phải atomic. |
| Week 6 | Deliverable approval, escrow release/refund, ready-made service purchase. | Không trộn ServiceOrder với ProjectEscrow. |
| Week 7 | End-to-end core flow, freeze API contract lần 1. | Frontend bắt đầu integration với backend thật. |
| Week 8 | Feature complete, hardening, edge cases, admin, notification. | Không thêm flow lớn sau tuần này. |
| Week 9 | Integration, testing, security fixing, bug fixing, optimization. | Không build feature mới. |
| Week 10 | Demo, deployment preparation, defense preparation. | Không refactor lớn, chỉ fix blocker. |


## 1.2 Đường găng backend

```text
Security + APIResponse + GlobalException
  → User + ClientProfile + ExpertProfile
    → Wallet baseline
      → JobPost
        → ExpertApplication / Proposal
          → ProjectInvitation
            → acceptInvitation transaction
              → Project + Contract + ProjectEscrow + ProjectConversation
                → fundEscrow transaction
                  → Project ACTIVE
                    → Deliverable submit/revision/approve
                      → approveDeliverable transaction
                        → Escrow RELEASED + Expert Wallet updated
                          → ClientProjectService + Review
```

```text
ExpertService
  → publish service
    → Client purchase
      → ServiceOrder + PaymentTransaction + Invoice
        → ServiceDelivery
          → ClientService + Review
```

## 1.3 Module rủi ro cao

| Module | Mức rủi ro | Nguyên nhân | Quy tắc kiểm soát |
| --- | --- | --- | --- |
| ProjectEscrow | P0 | Liên quan tiền, trạng thái Project, PaymentTransaction, Wallet. | Không cho controller set status trực tiếp; mọi flow qua EscrowWorkflowService. |
| Wallet | P0 | Dễ lệch balance khi release/refund/withdrawal/double request. | Ledger-based update, transaction lock, test concurrency. |
| Project lifecycle | P0 | Sai state làm vỡ deliverable, review, escrow. | StateTransitionService + invalid transition tests. |
| Deliverable approval | P0 | Approve phải release tiền và complete project atomically. | @Transactional bắt buộc; rollback test. |
| ServiceOrder purchase | P0 | Payment + invoice + order phải đồng bộ. | purchaseService transaction; không tạo order PAID nếu payment fail. |
| Conversation/Message | P1 | Participant validation và pagination dễ bị bỏ sót. | Query theo participant; không trả full history. |
| Admin/Dispute | P1 | Admin resolve có thể release/refund tiền. | Admin action phải audit đầy đủ và dùng workflow service. |
| Notification | P2 | Không critical money nhưng ảnh hưởng UX và integration. | Event table/NotificationService dùng chung; không hardcode rải rác. |


## 1.4 Dependency kỹ thuật phải build trước

- Authentication, Authorization, JWT, RBAC, APIResponse và GlobalException phải xong trước khi expose module nghiệp vụ.
- User/Profile/Wallet baseline phải có trước JobPost/Application/ServiceOrder vì mọi flow đều cần owner.
- JobPost/Application/Invitation phải hoàn thiện trước Project vì Project sinh ra từ accepted invitation.
- ProjectEscrow phải có trước Deliverable approval vì approval cần release escrow.
- PaymentTransaction/Invoice/Wallet phải có trước ServiceOrder purchase và Withdrawal.
- Notification có thể triển khai sau nhưng interface NotificationService phải có sớm để các module gọi thống nhất.

# 2. BẢN ĐỒ PHỤ THUỘC MODULE HOÀN CHỈNH

| Module | Phụ thuộc upstream | Module downstream | Tác động integration | Rủi ro nếu sai |
| --- | --- | --- | --- | --- |
| Authentication / Authorization | Không phụ thuộc module nghiệp vụ. | Tất cả module backend. | Mọi API phải có role/permission rõ ràng. | Sai security làm hở toàn hệ thống. |
| User / ClientProfile / ExpertProfile | Auth/JWT. | JobPost, Application, Service, Wallet, Review. | Định danh owner/participant. | Sai relationship làm ownership validation sai. |
| Wallet | User. | Escrow, ServiceOrder, Withdrawal, PaymentTransaction. | Tạo ví và quản lý balance/ledger. | Lệch tiền nếu không atomic. |
| JobPost | ClientProfile, User. | Application, Proposal, Invitation. | Nguồn bắt đầu custom project. | Nếu JobPostStatus sai thì invitation/project sai. |
| ExpertApplication / Proposal | JobPost, ExpertProfile. | Invitation, Proposal unlock/payment. | Expert nộp application/proposal. | Dễ lộ proposal nếu không check visibility. |
| Invitation | Application/Proposal, Client, Expert. | Project, Contract, ProjectEscrow, Conversation. | Chỉ khi ACCEPTED mới tạo Project. | Đường găng. |
| Project | Invitation, Contract, User. | Escrow, Deliverable, Dispute, Review, Conversation. | Trung tâm custom project. | Sai state transition phá toàn flow. |
| ProjectEscrow | Project, Wallet, PaymentTransaction. | Deliverable approval, Dispute resolution. | Giữ/release/refund tiền custom project. | Rủi ro P0. |
| Deliverable | Project ACTIVE. | Escrow release, ClientProjectService, Review. | Expert bàn giao và Client approve/revision. | Approve sai gây release tiền sai. |
| ExpertService | ExpertProfile. | ServiceOrder. | Ready-made service listing. | Sai publish rule làm Client mua service chưa sẵn sàng. |
| ServiceOrder | ExpertService, ClientProfile, PaymentTransaction. | ServiceDelivery, Invoice, ClientService, Review. | Ready-made service purchase flow. | Sai payment/order state gây lỗi tiền. |
| PaymentTransaction | Wallet, Escrow, Order. | Invoice, Admin financial dashboard. | Audit mọi giao dịch tiền. | Không đủ reference làm mất truy vết. |
| Invoice | PaymentTransaction, ServiceOrder/Project. | Client/Expert/Admin xem chứng từ. | Tạo chứng từ sau payment success. | Sai invoice làm frontend/admin thiếu dữ liệu. |
| WithdrawalRequest | Wallet, ExpertProfile. | Admin approval/payment. | Expert rút tiền. | Dễ rút vượt balance nếu không reserve. |
| Dispute | Project/ServiceOrder, Escrow/Payment. | Admin resolve, Wallet adjustment. | Xử lý tranh chấp. | Resolve sai gây mất tiền. |
| Notification | Tất cả business event. | Frontend notification center. | Thông báo trạng thái hệ thống. | Thiếu notification làm UX đứt luồng. |


## 2.1 Dependency hierarchy

```text
Level 0: Common Infrastructure
  - APIResponse
  - GlobalExceptionHandler
  - Validation
  - SecurityFilterChain
  - JWT provider/filter
  - BaseEntity / auditing

Level 1: Identity + Ownership
  - User
  - ClientProfile
  - ExpertProfile
  - Wallet baseline

Level 2: Marketplace Entry
  - JobPost
  - ExpertApplication
  - Proposal
  - ExpertService

Level 3: Negotiation / Purchase Trigger
  - ProjectInvitation
  - Proposal unlock/payment
  - ServiceOrder purchase

Level 4: Core Business Lifecycle
  - Project
  - Contract
  - ProjectEscrow
  - Conversation
  - ServiceDelivery

Level 5: Completion / Finance / Trust
  - Deliverable
  - ClientProjectService
  - ClientService
  - Review
  - Invoice
  - WithdrawalRequest
  - Dispute
  - Notification
```

# 3. ROADMAP TRIỂN KHAI THEO TUẦN — WEEK 3 ĐẾN WEEK 10

## Week 3 — Khóa nền tảng backend và chuẩn hợp đồng dữ liệu

**Mục tiêu tuần:** Khóa nền tảng backend và chuẩn hợp đồng dữ liệu.  

**Mục tiêu kiến trúc:** Chuẩn hóa security foundation, response contract, exception flow, User/Profile/Wallet baseline để các module nghiệp vụ không tự định nghĩa lại hạ tầng.

### Phân công Lập trình viên Backend 1

| Nhóm việc | Task cụ thể |
| --- | --- |
| Entity/Enum | Rà soát User, ClientProfile, ExpertProfile, Role, UserStatus; khóa tên field dùng chung. |
| Security | Hoàn thiện JWT authentication, SecurityFilterChain, RBAC theo endpoint group. |
| Common | Chuẩn hóa APIResponse<T>, ErrorResponse, GlobalExceptionHandler, Validation message. |
| Service | Tách AuthenticationService, UserService, ProfileService; không để controller xử lý logic. |
| Testing | Test login/register, invalid token, expired token, role denied, validation error. |


### Phân công Lập trình viên Backend 2

| Nhóm việc | Task cụ thể |
| --- | --- |
| Entity | Tạo JobPost, JobPostStatus, ExpertApplication, ApplicationStatus, Proposal, ProposalStatus. |
| Repository | Tạo query theo owner, status, public listing, search keyword. |
| Service | Tạo skeleton createJobPost, publishJobPost, applyJobPost, withdrawApplication. |
| DTO/Mapper | Tách request/response cho JobPost và Application; response không trả dữ liệu nhạy cảm. |
| Testing | Test Client tạo job, Expert apply, duplicate apply bị chặn. |


### Phân công Lập trình viên Backend 3

| Nhóm việc | Task cụ thể |
| --- | --- |
| Entity | Tạo Wallet, WalletType, WalletTransactionType baseline, PaymentTransaction, PaymentStatus, Invoice skeleton. |
| Service | Tạo WalletService với createWalletForUser, getBalance, hold/release skeleton chưa expose public. |
| Repository | Query wallet by user, transaction by owner, invoice by order/project. |
| DTO | WalletResponse, PaymentTransactionResponse, InvoiceResponse. |
| Testing | Test tạo wallet khi register, không cho số dư âm. |


### Checklist kỹ thuật trong tuần

- [ ] Entity/Enum tạo đúng tên, đúng relationship, đúng owner.
- [ ] Repository có query theo owner/status và pagination nếu listing.
- [ ] DTO request/response không leak field nhạy cảm.
- [ ] Mapper không tự map quan hệ nguy hiểm nếu cần service gán owner.
- [ ] Service có validation, state transition, ownership check.
- [ ] Controller mỏng, không chứa business logic.
- [ ] Transaction boundary đặt ở workflow/service layer.
- [ ] DB migration hoặc schema update được review trước merge.
- [ ] Unit test + integration test cho flow chính và invalid flow.
- [ ] PR merge qua develop sau code review.

| Hạng mục | Nội dung |
| --- | --- |
| Mốc API | Auth/Profile/User/JobPost/Application/Wallet read-only baseline. |
| Phụ thuộc frontend | Frontend có thể bắt đầu login/register/profile/job listing bằng API contract ổn định. |
| Phụ thuộc đang chặn | Không merge module downstream nếu upstream chưa ổn định. |
| Cảnh báo rủi ro | Nếu Week 3 không khóa APIResponse, exception và User ownership, toàn bộ module sau sẽ lệch contract. |
| Kết quả kỳ vọng | Code chạy được trên develop, Postman collection cập nhật, test chính pass, không còn endpoint tạm cho flow chính. |


## Week 4 — Hoàn thiện pre-project flow: JobPost → Application/Proposal → Invitation

**Mục tiêu tuần:** Hoàn thiện pre-project flow: JobPost → Application/Proposal → Invitation.  

**Mục tiêu kiến trúc:** Hoàn thiện luồng trước khi tạo Project, bảo đảm Project chưa được sinh ra cho đến khi Expert accept invitation.

### Phân công Lập trình viên Backend 1

| Nhóm việc | Task cụ thể |
| --- | --- |
| Authorization | Bổ sung endpoint permission matrix cho Client/Expert/Admin. |
| Ownership | Viết helper validateClientOwner, validateExpertOwner, validateAdminAccess. |
| Admin core | Tạo admin list users, update user status, inspect user profile. |
| Audit | Thêm createdAt, updatedAt, createdBy/updatedBy nếu ERD cho phép; thống nhất timestamp handling. |
| Testing | Test role/ownership cho toàn bộ endpoint Week 3-4. |


### Phân công Lập trình viên Backend 2

| Nhóm việc | Task cụ thể |
| --- | --- |
| JobPost | Finalize create/update/publish/close/cancel JobPost. |
| Application | Finalize ExpertApplication lifecycle: SUBMITTED, WITHDRAWN, REJECTED, SHORTLISTED. |
| Proposal | Tạo Proposal entity/DTO/API nếu tách khỏi application; khóa proposal visibility. |
| Invitation | Tạo ProjectInvitation entity, InvitationStatus, send/accept/reject/expire. |
| Notification | Gọi notification event cho apply, shortlist, invite, accept, reject. |


### Phân công Lập trình viên Backend 3

| Nhóm việc | Task cụ thể |
| --- | --- |
| ExpertService | Tạo ExpertService, ServiceStatus, package fields, publish/unpublish. |
| Payment | Chuẩn bị ProposalUnlockPayment nếu SRS yêu cầu client trả phí mở proposal. |
| Invoice | Invoice numbering convention, invoice status, invoice response baseline. |
| Repository | Query service public listing, service owner dashboard. |
| Testing | Test Expert publish service, Client xem service, owner edit service. |


### Checklist kỹ thuật trong tuần

- [ ] Entity/Enum tạo đúng tên, đúng relationship, đúng owner.
- [ ] Repository có query theo owner/status và pagination nếu listing.
- [ ] DTO request/response không leak field nhạy cảm.
- [ ] Mapper không tự map quan hệ nguy hiểm nếu cần service gán owner.
- [ ] Service có validation, state transition, ownership check.
- [ ] Controller mỏng, không chứa business logic.
- [ ] Transaction boundary đặt ở workflow/service layer.
- [ ] DB migration hoặc schema update được review trước merge.
- [ ] Unit test + integration test cho flow chính và invalid flow.
- [ ] PR merge qua develop sau code review.

| Hạng mục | Nội dung |
| --- | --- |
| Mốc API | JobPost/Application/Proposal/Invitation/ExpertService public listing. |
| Phụ thuộc frontend | Frontend có thể làm màn hình browse jobs, apply, proposal list, invitation inbox. |
| Phụ thuộc đang chặn | Không merge module downstream nếu upstream chưa ổn định. |
| Cảnh báo rủi ro | Tạo Project quá sớm ở tuần này sẽ phá toàn bộ escrow lifecycle về sau. |
| Kết quả kỳ vọng | Code chạy được trên develop, Postman collection cập nhật, test chính pass, không còn endpoint tạm cho flow chính. |


## Week 5 — Project creation transaction và Escrow funding

**Mục tiêu tuần:** Project creation transaction và Escrow funding.  

**Mục tiêu kiến trúc:** Khóa transaction acceptInvitation và fundEscrow; Project chỉ ACTIVE khi escrow HELD.

### Phân công Lập trình viên Backend 1

| Nhóm việc | Task cụ thể |
| --- | --- |
| Security | Áp dụng ownership validation cho Project/Conversation/Escrow endpoint. |
| Common | Chuẩn hóa BaseEntity, BaseResponse, PageResponse nếu chưa xong. |
| Exception | Tạo BusinessException theo mã lỗi: INVALID_STATUS, FORBIDDEN_OWNER, INSUFFICIENT_BALANCE. |
| Integration | Review transaction boundary của Dev2/Dev3 để không update tiền ngoài service. |
| Testing | Security matrix cho Project/Escrow endpoints. |


### Phân công Lập trình viên Backend 2

| Nhóm việc | Task cụ thể |
| --- | --- |
| Project | Tạo Project, ProjectStatus, ProjectMember/participant nếu ERD có. |
| Contract | Tạo Contract entity + contract status + snapshot agreedAmount/scope/deadline. |
| Conversation | Tạo ProjectConversation, DirectConversation, Message; participant validation. |
| Transaction | acceptInvitation(): tạo Project + Contract + ProjectEscrow + ProjectConversation trong cùng transaction. |
| Controller | ProjectController: get my projects, detail, cancel pending funding, timeline. |


### Phân công Lập trình viên Backend 3

| Nhóm việc | Task cụ thể |
| --- | --- |
| Escrow | Tạo ProjectEscrow, EscrowStatus, escrow amount fields. |
| PaymentTransaction | Implement fundEscrow transaction: create payment, hold amount, set escrow HELD. |
| Wallet | Wallet debit/hold/release/refund methods; không cho balance âm. |
| Invoice | Generate invoice cho escrow funding nếu SRS yêu cầu. |
| Testing | Rollback test: payment fail thì Project không ACTIVE, escrow không HELD, wallet không trừ sai. |


### Checklist kỹ thuật trong tuần

- [ ] Entity/Enum tạo đúng tên, đúng relationship, đúng owner.
- [ ] Repository có query theo owner/status và pagination nếu listing.
- [ ] DTO request/response không leak field nhạy cảm.
- [ ] Mapper không tự map quan hệ nguy hiểm nếu cần service gán owner.
- [ ] Service có validation, state transition, ownership check.
- [ ] Controller mỏng, không chứa business logic.
- [ ] Transaction boundary đặt ở workflow/service layer.
- [ ] DB migration hoặc schema update được review trước merge.
- [ ] Unit test + integration test cho flow chính và invalid flow.
- [ ] PR merge qua develop sau code review.

| Hạng mục | Nội dung |
| --- | --- |
| Mốc API | Project/Contract/Conversation/Escrow funding core. |
| Phụ thuộc frontend | Frontend có thể làm project detail, conversation, fund escrow button. |
| Phụ thuộc đang chặn | Không merge module downstream nếu upstream chưa ổn định. |
| Cảnh báo rủi ro | Đây là critical path. Nếu fundEscrow không atomic, sẽ sinh lỗi mất tiền/lệch trạng thái. |
| Kết quả kỳ vọng | Code chạy được trên develop, Postman collection cập nhật, test chính pass, không còn endpoint tạm cho flow chính. |


## Week 6 — Deliverable, escrow release/refund và ready-made service order

**Mục tiêu tuần:** Deliverable, escrow release/refund và ready-made service order.  

**Mục tiêu kiến trúc:** Hoàn thiện hai lifecycle thanh toán chính: custom project và ready-made service.

### Phân công Lập trình viên Backend 1

| Nhóm việc | Task cụ thể |
| --- | --- |
| Security | Validate participant cho Deliverable/Message/Review. |
| Admin | Admin inspect project/order/payment/dispute read-only. |
| Validation | Chuẩn hóa validation deadline, amount, file metadata, review rating. |
| Testing | Permission tests cho Client/Expert/Admin ở flow deliverable và service order. |
| Code quality | Remove hardcode status, gom status transition vào service. |


### Phân công Lập trình viên Backend 2

| Nhóm việc | Task cụ thể |
| --- | --- |
| Deliverable | Tạo Deliverable, DeliverableStatus, submit/requestRevision/approve. |
| State | Chỉ cho submit khi Project ACTIVE; approve khi Deliverable SUBMITTED hoặc REVISION_SUBMITTED. |
| Dispute | Tạo Dispute entity/API: open, comment, resolve refund/release. |
| Notification | Notify deliverable submitted, revision requested, approved, dispute opened/resolved. |
| Testing | E2E custom project: apply → invite → accept → fund → submit → approve. |


### Phân công Lập trình viên Backend 3

| Nhóm việc | Task cụ thể |
| --- | --- |
| ServiceOrder | Tạo ServiceOrder, OrderStatus, purchase transaction. |
| ServiceDelivery | Tạo ServiceDelivery, DeliveryStatus, submit/accept/revision. |
| ClientService | Tạo ClientService khi ServiceOrder completed. |
| Payment | purchaseService(): PaymentTransaction + Invoice + ServiceOrder trong cùng transaction. |
| Testing | Rollback service purchase, delivery accept, wallet credit. |


### Checklist kỹ thuật trong tuần

- [ ] Entity/Enum tạo đúng tên, đúng relationship, đúng owner.
- [ ] Repository có query theo owner/status và pagination nếu listing.
- [ ] DTO request/response không leak field nhạy cảm.
- [ ] Mapper không tự map quan hệ nguy hiểm nếu cần service gán owner.
- [ ] Service có validation, state transition, ownership check.
- [ ] Controller mỏng, không chứa business logic.
- [ ] Transaction boundary đặt ở workflow/service layer.
- [ ] DB migration hoặc schema update được review trước merge.
- [ ] Unit test + integration test cho flow chính và invalid flow.
- [ ] PR merge qua develop sau code review.

| Hạng mục | Nội dung |
| --- | --- |
| Mốc API | Deliverable/Dispute/ServiceOrder/ServiceDelivery/ClientService. |
| Phụ thuộc frontend | Frontend có thể làm workspace custom project và purchased service dashboard. |
| Phụ thuộc đang chặn | Không merge module downstream nếu upstream chưa ổn định. |
| Cảnh báo rủi ro | Ready-made service không được dùng ProjectEscrow; nếu trộn flow sẽ rất khó debug. |
| Kết quả kỳ vọng | Code chạy được trên develop, Postman collection cập nhật, test chính pass, không còn endpoint tạm cho flow chính. |


## Week 7 — Hoàn thiện core backend end-to-end và freeze API contract

**Mục tiêu tuần:** Hoàn thiện core backend end-to-end và freeze API contract.  

**Mục tiêu kiến trúc:** Nối đầy đủ custom project + ready-made service + notification + review + admin inspection; khóa response cho frontend.

### Phân công Lập trình viên Backend 1

| Nhóm việc | Task cụ thể |
| --- | --- |
| Admin | User management, user status update, dashboard counts, moderation basics. |
| Security | Full endpoint protection review; endpoint nào thiếu ownership phải fix ngay. |
| API contract | Freeze common response fields, error code, pagination format. |
| Testing | Security matrix: Guest/Client/Expert/Admin trên từng module. |
| Quality | Remove unused endpoints, remove temporary code, log chuẩn. |


### Phân công Lập trình viên Backend 2

| Nhóm việc | Task cụ thể |
| --- | --- |
| Custom flow | Finalize JobPost/Application/Invitation/Project/Deliverable/Dispute end-to-end. |
| Conversation | Direct và Project conversation integration; message pagination. |
| Notification | Trigger đủ event trong custom flow. |
| API freeze | Freeze Project/Deliverable/Dispute response DTO. |
| Testing | E2E script custom project cho Postman/Newman. |


### Phân công Lập trình viên Backend 3

| Nhóm việc | Task cụ thể |
| --- | --- |
| Service flow | Finalize ExpertService/ServiceOrder/ServiceDelivery/ClientService. |
| Wallet | Finalize wallet ledger, transaction list, balance response. |
| Withdrawal | Create withdrawal request, admin approve/reject/mark paid. |
| Review | Review cho Project và ServiceOrder; chặn review trùng. |
| Testing | E2E ready-made service + withdrawal script. |


### Checklist kỹ thuật trong tuần

- [ ] Entity/Enum tạo đúng tên, đúng relationship, đúng owner.
- [ ] Repository có query theo owner/status và pagination nếu listing.
- [ ] DTO request/response không leak field nhạy cảm.
- [ ] Mapper không tự map quan hệ nguy hiểm nếu cần service gán owner.
- [ ] Service có validation, state transition, ownership check.
- [ ] Controller mỏng, không chứa business logic.
- [ ] Transaction boundary đặt ở workflow/service layer.
- [ ] DB migration hoặc schema update được review trước merge.
- [ ] Unit test + integration test cho flow chính và invalid flow.
- [ ] PR merge qua develop sau code review.

| Hạng mục | Nội dung |
| --- | --- |
| Mốc API | Full core backend gần hoàn thiện; frontend contract freeze lần 1. |
| Phụ thuộc frontend | Frontend bắt đầu integration thật theo API stable, không dựa mock cho flow chính. |
| Phụ thuộc đang chặn | Không merge module downstream nếu upstream chưa ổn định. |
| Cảnh báo rủi ro | Nếu Week 7 chưa end-to-end, Week 9 sẽ bị biến thành tuần code mới, sai timeline. |
| Kết quả kỳ vọng | Code chạy được trên develop, Postman collection cập nhật, test chính pass, không còn endpoint tạm cho flow chính. |


## Week 8 — Feature complete backend và hardening

**Mục tiêu tuần:** Feature complete backend và hardening.  

**Mục tiêu kiến trúc:** Hoàn thiện Admin, edge cases, notification coverage, audit logging, filter/search, pagination, export nếu cần.

### Phân công Lập trình viên Backend 1

| Nhóm việc | Task cụ thể |
| --- | --- |
| Hardening | Security hardening, CORS, password policy, token refresh nếu scope có. |
| Admin | Admin moderation, disable user, inspect financial transaction. |
| Exception | Chuẩn hóa tất cả error code; không còn RuntimeException thô. |
| Audit | Review audit log/createdBy/updatedBy cho business action quan trọng. |
| Testing | Regression security + validation. |


### Phân công Lập trình viên Backend 2

| Nhóm việc | Task cụ thể |
| --- | --- |
| Custom edge cases | Cancel job, expired invitation, project cancel before funding, revision limit. |
| Dispute | Admin resolve: release, refund, partial nếu scope có. |
| Search/filter | JobPost, Project, Application, Dispute filters. |
| Performance | Message pagination, conversation indexes. |
| Testing | Regression custom project edge cases. |


### Phân công Lập trình viên Backend 3

| Nhóm việc | Task cụ thể |
| --- | --- |
| Financial edge cases | Double payment prevention, idempotency key nếu có, withdrawal edge cases. |
| Invoice | Finalize invoice generation and lookup. |
| Notification | Notification read/unread, list, mark read. |
| Performance | Wallet transaction pagination and indexing. |
| Testing | Financial consistency regression. |


### Checklist kỹ thuật trong tuần

- [ ] Entity/Enum tạo đúng tên, đúng relationship, đúng owner.
- [ ] Repository có query theo owner/status và pagination nếu listing.
- [ ] DTO request/response không leak field nhạy cảm.
- [ ] Mapper không tự map quan hệ nguy hiểm nếu cần service gán owner.
- [ ] Service có validation, state transition, ownership check.
- [ ] Controller mỏng, không chứa business logic.
- [ ] Transaction boundary đặt ở workflow/service layer.
- [ ] DB migration hoặc schema update được review trước merge.
- [ ] Unit test + integration test cho flow chính và invalid flow.
- [ ] PR merge qua develop sau code review.

| Hạng mục | Nội dung |
| --- | --- |
| Mốc API | Backend feature complete; chỉ còn bug/security/performance fix sau tuần này. |
| Phụ thuộc frontend | Frontend integration gần đầy đủ, chỉ còn chỉnh lỗi contract nhỏ. |
| Phụ thuộc đang chặn | Không merge module downstream nếu upstream chưa ổn định. |
| Cảnh báo rủi ro | Không được thêm flow lớn mới sau Week 8 nếu không có quyết định cắt scope. |
| Kết quả kỳ vọng | Code chạy được trên develop, Postman collection cập nhật, test chính pass, không còn endpoint tạm cho flow chính. |


## Week 9 — Integration, testing, optimization, security fixing, bug fixing

**Mục tiêu tuần:** Integration, testing, optimization, security fixing, bug fixing.  

**Mục tiêu kiến trúc:** Không build feature lớn. Tập trung ổn định toàn hệ thống và fix theo evidence từ test.

### Phân công Lập trình viên Backend 1

| Nhóm việc | Task cụ thể |
| --- | --- |
| Security test | Run endpoint permission matrix, fix forbidden/unauthorized mismatch. |
| Validation test | Run invalid request suite; đảm bảo error response đồng nhất. |
| Performance | Check N+1 ở user/profile/admin listings. |
| Code review | Review dependency direction và circular service. |
| Bug fixing | Fix blocker theo priority P0/P1. |


### Phân công Lập trình viên Backend 2

| Nhóm việc | Task cụ thể |
| --- | --- |
| E2E test | Run custom project full script nhiều lần với dữ liệu khác nhau. |
| Concurrency | Test duplicate accept, duplicate approve, double submit. |
| Performance | Optimize project/detail queries, message pagination. |
| Bug fixing | Fix state transition bugs. |
| Integration | Support frontend custom project flow. |


### Phân công Lập trình viên Backend 3

| Nhóm việc | Task cụ thể |
| --- | --- |
| E2E test | Run ready-made service, wallet, withdrawal, invoice regression. |
| Consistency | Verify wallet/escrow/payment ledger after every test. |
| Concurrency | Test double purchase/double release/double withdrawal. |
| Bug fixing | Fix transaction and invoice bugs. |
| Integration | Support frontend service/payment screens. |


### Checklist kỹ thuật trong tuần

- [ ] Entity/Enum tạo đúng tên, đúng relationship, đúng owner.
- [ ] Repository có query theo owner/status và pagination nếu listing.
- [ ] DTO request/response không leak field nhạy cảm.
- [ ] Mapper không tự map quan hệ nguy hiểm nếu cần service gán owner.
- [ ] Service có validation, state transition, ownership check.
- [ ] Controller mỏng, không chứa business logic.
- [ ] Transaction boundary đặt ở workflow/service layer.
- [ ] DB migration hoặc schema update được review trước merge.
- [ ] Unit test + integration test cho flow chính và invalid flow.
- [ ] PR merge qua develop sau code review.

| Hạng mục | Nội dung |
| --- | --- |
| Mốc API | API freeze final; chỉ hotfix không đổi contract nếu không bắt buộc. |
| Phụ thuộc frontend | Frontend chạy test end-to-end với backend thật. |
| Phụ thuộc đang chặn | Không merge module downstream nếu upstream chưa ổn định. |
| Cảnh báo rủi ro | Nếu còn đổi DTO response ở Week 9, frontend sẽ vỡ hàng loạt. |
| Kết quả kỳ vọng | Code chạy được trên develop, Postman collection cập nhật, test chính pass, không còn endpoint tạm cho flow chính. |


## Week 10 — Demo, polishing, deployment preparation, defense preparation

**Mục tiêu tuần:** Demo, polishing, deployment preparation, defense preparation.  

**Mục tiêu kiến trúc:** Ổn định release candidate, chuẩn bị dữ liệu demo, script demo, deployment config, tài liệu bảo vệ.

### Phân công Lập trình viên Backend 1

| Nhóm việc | Task cụ thể |
| --- | --- |
| Deployment | Profile dev/prod, env variables, security config for deploy. |
| Demo data | Seed users Client/Expert/Admin, sample profiles. |
| Documentation | Auth/security/role explanation for defense. |
| Monitoring | Log format, error visibility, health check. |
| Support | Fix demo blocker only. |


### Phân công Lập trình viên Backend 2

| Nhóm việc | Task cụ thể |
| --- | --- |
| Demo flow | Seed job posts, applications, invitation, active/completed project. |
| Script | Chuẩn bị custom project demo script từ đầu đến cuối. |
| Defense | Explain state transition custom project. |
| Polish | Clean response message, sorting, timeline response. |
| Support | Fix demo blocker only. |


### Phân công Lập trình viên Backend 3

| Nhóm việc | Task cụ thể |
| --- | --- |
| Demo flow | Seed expert services, orders, deliveries, invoices, wallet transactions. |
| Script | Chuẩn bị ready-made service + wallet/withdrawal demo script. |
| Defense | Explain transaction consistency and rollback. |
| Polish | Clean invoice/wallet responses. |
| Support | Fix demo blocker only. |


### Checklist kỹ thuật trong tuần

- [ ] Entity/Enum tạo đúng tên, đúng relationship, đúng owner.
- [ ] Repository có query theo owner/status và pagination nếu listing.
- [ ] DTO request/response không leak field nhạy cảm.
- [ ] Mapper không tự map quan hệ nguy hiểm nếu cần service gán owner.
- [ ] Service có validation, state transition, ownership check.
- [ ] Controller mỏng, không chứa business logic.
- [ ] Transaction boundary đặt ở workflow/service layer.
- [ ] DB migration hoặc schema update được review trước merge.
- [ ] Unit test + integration test cho flow chính và invalid flow.
- [ ] PR merge qua develop sau code review.

| Hạng mục | Nội dung |
| --- | --- |
| Mốc API | Release candidate ổn định cho demo. |
| Phụ thuộc frontend | Frontend chỉ polish UI và demo flow, không đổi backend contract. |
| Phụ thuộc đang chặn | Không merge module downstream nếu upstream chưa ổn định. |
| Cảnh báo rủi ro | Không merge refactor lớn trong Week 10. |
| Kết quả kỳ vọng | Code chạy được trên develop, Postman collection cập nhật, test chính pass, không còn endpoint tạm cho flow chính. |


# 4. KẾ HOẠCH THỰC THI HẰNG NGÀY

## Week 3 — Kế hoạch theo ngày

| Ngày | Thứ tự thực thi |
| --- | --- |
| Day 1 | Khóa scope trong tuần, review dependency, tạo branch feature, thống nhất entity/DTO/enum trước khi code service. |
| Day 2 | Code entity, enum, repository, migration/schema update; chạy app và verify relationship. |
| Day 3 | Code service + mapper + validation + transaction chính; không expose endpoint nếu service chưa có ownership check. |
| Day 4 | Code controller + DTO response + APIResponse; cập nhật Postman; chạy test happy path và invalid path. |
| Day 5 | Integration giữa 3 dev, fix conflict, code review, merge develop, cập nhật API contract cho frontend. |
| Buffer cuối tuần | Fix bug blocker, bổ sung test thiếu, cleanup naming, chuẩn bị task tuần sau; không nhồi feature mới nếu chưa review. |


**Thứ tự merge:** Dev 1 merge common/security trước → Dev 2/Dev 3 rebase develop → merge module nghiệp vụ → chạy regression ngắn trước khi chốt tuần.

## Week 4 — Kế hoạch theo ngày

| Ngày | Thứ tự thực thi |
| --- | --- |
| Day 1 | Khóa scope trong tuần, review dependency, tạo branch feature, thống nhất entity/DTO/enum trước khi code service. |
| Day 2 | Code entity, enum, repository, migration/schema update; chạy app và verify relationship. |
| Day 3 | Code service + mapper + validation + transaction chính; không expose endpoint nếu service chưa có ownership check. |
| Day 4 | Code controller + DTO response + APIResponse; cập nhật Postman; chạy test happy path và invalid path. |
| Day 5 | Integration giữa 3 dev, fix conflict, code review, merge develop, cập nhật API contract cho frontend. |
| Buffer cuối tuần | Fix bug blocker, bổ sung test thiếu, cleanup naming, chuẩn bị task tuần sau; không nhồi feature mới nếu chưa review. |


**Thứ tự merge:** Dev 1 merge common/security trước → Dev 2/Dev 3 rebase develop → merge module nghiệp vụ → chạy regression ngắn trước khi chốt tuần.

## Week 5 — Kế hoạch theo ngày

| Ngày | Thứ tự thực thi |
| --- | --- |
| Day 1 | Khóa scope trong tuần, review dependency, tạo branch feature, thống nhất entity/DTO/enum trước khi code service. |
| Day 2 | Code entity, enum, repository, migration/schema update; chạy app và verify relationship. |
| Day 3 | Code service + mapper + validation + transaction chính; không expose endpoint nếu service chưa có ownership check. |
| Day 4 | Code controller + DTO response + APIResponse; cập nhật Postman; chạy test happy path và invalid path. |
| Day 5 | Integration giữa 3 dev, fix conflict, code review, merge develop, cập nhật API contract cho frontend. |
| Buffer cuối tuần | Fix bug blocker, bổ sung test thiếu, cleanup naming, chuẩn bị task tuần sau; không nhồi feature mới nếu chưa review. |


**Thứ tự merge:** Dev 1 merge common/security trước → Dev 2/Dev 3 rebase develop → merge module nghiệp vụ → chạy regression ngắn trước khi chốt tuần.

## Week 6 — Kế hoạch theo ngày

| Ngày | Thứ tự thực thi |
| --- | --- |
| Day 1 | Khóa scope trong tuần, review dependency, tạo branch feature, thống nhất entity/DTO/enum trước khi code service. |
| Day 2 | Code entity, enum, repository, migration/schema update; chạy app và verify relationship. |
| Day 3 | Code service + mapper + validation + transaction chính; không expose endpoint nếu service chưa có ownership check. |
| Day 4 | Code controller + DTO response + APIResponse; cập nhật Postman; chạy test happy path và invalid path. |
| Day 5 | Integration giữa 3 dev, fix conflict, code review, merge develop, cập nhật API contract cho frontend. |
| Buffer cuối tuần | Fix bug blocker, bổ sung test thiếu, cleanup naming, chuẩn bị task tuần sau; không nhồi feature mới nếu chưa review. |


**Thứ tự merge:** Dev 1 merge common/security trước → Dev 2/Dev 3 rebase develop → merge module nghiệp vụ → chạy regression ngắn trước khi chốt tuần.

## Week 7 — Kế hoạch theo ngày

| Ngày | Thứ tự thực thi |
| --- | --- |
| Day 1 | Khóa scope trong tuần, review dependency, tạo branch feature, thống nhất entity/DTO/enum trước khi code service. |
| Day 2 | Code entity, enum, repository, migration/schema update; chạy app và verify relationship. |
| Day 3 | Code service + mapper + validation + transaction chính; không expose endpoint nếu service chưa có ownership check. |
| Day 4 | Code controller + DTO response + APIResponse; cập nhật Postman; chạy test happy path và invalid path. |
| Day 5 | Integration giữa 3 dev, fix conflict, code review, merge develop, cập nhật API contract cho frontend. |
| Buffer cuối tuần | Fix bug blocker, bổ sung test thiếu, cleanup naming, chuẩn bị task tuần sau; không nhồi feature mới nếu chưa review. |


**Thứ tự merge:** Dev 1 merge common/security trước → Dev 2/Dev 3 rebase develop → merge module nghiệp vụ → chạy regression ngắn trước khi chốt tuần.

## Week 8 — Kế hoạch theo ngày

| Ngày | Thứ tự thực thi |
| --- | --- |
| Day 1 | Khóa scope trong tuần, review dependency, tạo branch feature, thống nhất entity/DTO/enum trước khi code service. |
| Day 2 | Code entity, enum, repository, migration/schema update; chạy app và verify relationship. |
| Day 3 | Code service + mapper + validation + transaction chính; không expose endpoint nếu service chưa có ownership check. |
| Day 4 | Code controller + DTO response + APIResponse; cập nhật Postman; chạy test happy path và invalid path. |
| Day 5 | Integration giữa 3 dev, fix conflict, code review, merge develop, cập nhật API contract cho frontend. |
| Buffer cuối tuần | Fix bug blocker, bổ sung test thiếu, cleanup naming, chuẩn bị task tuần sau; không nhồi feature mới nếu chưa review. |


**Thứ tự merge:** Dev 1 merge common/security trước → Dev 2/Dev 3 rebase develop → merge module nghiệp vụ → chạy regression ngắn trước khi chốt tuần.

## Week 9 — Kế hoạch theo ngày

| Ngày | Thứ tự thực thi |
| --- | --- |
| Day 1 | Khóa scope trong tuần, review dependency, tạo branch feature, thống nhất entity/DTO/enum trước khi code service. |
| Day 2 | Code entity, enum, repository, migration/schema update; chạy app và verify relationship. |
| Day 3 | Code service + mapper + validation + transaction chính; không expose endpoint nếu service chưa có ownership check. |
| Day 4 | Code controller + DTO response + APIResponse; cập nhật Postman; chạy test happy path và invalid path. |
| Day 5 | Integration giữa 3 dev, fix conflict, code review, merge develop, cập nhật API contract cho frontend. |
| Buffer cuối tuần | Fix bug blocker, bổ sung test thiếu, cleanup naming, chuẩn bị task tuần sau; không nhồi feature mới nếu chưa review. |


**Thứ tự merge:** Dev 1 merge common/security trước → Dev 2/Dev 3 rebase develop → merge module nghiệp vụ → chạy regression ngắn trước khi chốt tuần.

## Week 10 — Kế hoạch theo ngày

| Ngày | Thứ tự thực thi |
| --- | --- |
| Day 1 | Khóa scope trong tuần, review dependency, tạo branch feature, thống nhất entity/DTO/enum trước khi code service. |
| Day 2 | Code entity, enum, repository, migration/schema update; chạy app và verify relationship. |
| Day 3 | Code service + mapper + validation + transaction chính; không expose endpoint nếu service chưa có ownership check. |
| Day 4 | Code controller + DTO response + APIResponse; cập nhật Postman; chạy test happy path và invalid path. |
| Day 5 | Integration giữa 3 dev, fix conflict, code review, merge develop, cập nhật API contract cho frontend. |
| Buffer cuối tuần | Fix bug blocker, bổ sung test thiếu, cleanup naming, chuẩn bị task tuần sau; không nhồi feature mới nếu chưa review. |


**Thứ tự merge:** Dev 1 merge common/security trước → Dev 2/Dev 3 rebase develop → merge module nghiệp vụ → chạy regression ngắn trước khi chốt tuần.

# 5. CHIẾN LƯỢC OWNERSHIP MODULE ĐẦY ĐỦ

## 5.1 Ownership theo developer

| Developer | Module sở hữu | Entity chính | DTO/Mapper/Service/Controller sở hữu | Không tự ý sửa |
| --- | --- | --- | --- | --- |
| Lập trình viên Backend 1 | Auth, Security, User, ClientProfile, ExpertProfile, Admin core, GlobalException, APIResponse, Validation. | User, ClientProfile, ExpertProfile, Role, UserStatus. | Auth DTO, User/Profile DTO, SecurityConfig, AuthenticationService, UserService, ProfileService, AdminUserController. | ProjectEscrow, ServiceOrder, PaymentTransaction money logic. |
| Lập trình viên Backend 2 | JobPost, ExpertApplication, Proposal, Invitation, Conversation, Message, Project, Contract, ProjectEscrow coordination, Deliverable, Dispute. | JobPost, ExpertApplication, Proposal, ProjectInvitation, Project, Contract, Conversation, Message, Deliverable, Dispute. | JobPost/Project/Deliverable DTO, mapper, service, controller. | Wallet balance update trực tiếp; phải gọi Dev3 service. |
| Lập trình viên Backend 3 | ExpertService, ServiceOrder, ServiceDelivery, ClientService, ClientProjectService, Wallet, PaymentTransaction, Invoice, Review, Notification, Withdrawal. | ExpertService, ServiceOrder, ServiceDelivery, ClientService, ClientProjectService, Wallet, PaymentTransaction, Invoice, Review, Notification, WithdrawalRequest. | ServiceOrder/Wallet/Payment/Notification DTO, mapper, service, controller. | Project lifecycle status; phải gọi Dev2 workflow khi liên quan Project. |


## 5.2 Quy tắc sửa entity shared

- Entity shared như User, Project, Wallet, PaymentTransaction, Review chỉ được sửa qua PR có reviewer là owner module.
- Không thêm field mới vào entity nếu chưa cập nhật DTO, mapper, migration, test và frontend contract.
- Không đổi tên enum sau freeze nếu chưa có migration và mapping backward compatibility.
- Relationship hai chiều chỉ thêm khi thật sự cần; tránh vòng lặp JSON bằng response DTO, không dùng entity trả trực tiếp.
- Mapper không được tự tạo entity owner mới; owner phải lấy từ authenticated user trong service.

## 5.3 DTO ownership strategy

| Loại DTO | Người chịu trách nhiệm | Rule |
| --- | --- | --- |
| Request DTO | Owner module tạo và maintain. | Có validation annotation rõ ràng; không nhận userId từ client nếu có thể lấy từ token. |
| Response DTO | Owner module tạo; Dev1 review common fields. | Không trả passwordHash, internal note, payment secret, audit field nhạy cảm. |
| PageResponse | Dev1 sở hữu. | Dùng chung cho listing endpoint; không mỗi module tự tạo format. |
| Admin DTO | Dev1 phối hợp owner module. | Có thể trả thêm thông tin vận hành nhưng không leak dữ liệu bảo mật. |
| Event/Notification DTO | Dev3 sở hữu. | Tên event thống nhất, payload nhỏ, không nhét full entity. |


# 6. QUẢN LÝ LIFECYCLE ENTITY HOÀN CHỈNH

## 6.1 JobPost

| Hạng mục | Quy định |
| --- | --- |
| Lifecycle states | DRAFT, PUBLISHED, CLOSED, CANCELLED, EXPIRED |
| Valid transitions | DRAFT → PUBLISHED; PUBLISHED → CLOSED/CANCELLED/EXPIRED; CLOSED không quay lại PUBLISHED nếu đã chọn Expert. |
| Invalid transitions | Guest tạo job; Expert sửa job; PUBLISHED thiếu budget/deadline; CANCELLED sau khi Project đã tạo. |
| Transaction requirements | create/update/publish có thể transaction ngắn; select Expert phải transaction cùng Invitation. |
| Rollback requirements | Nếu send invitation fail thì không đánh dấu application selected. |
| Notification triggers | Job published, Expert applied, Expert selected, job closed/cancelled. |
| Audit logging requirements | createdBy Client, updatedBy Client/Admin, publishedAt, closedAt. |


## 6.2 ExpertApplication

| Hạng mục | Quy định |
| --- | --- |
| Lifecycle states | SUBMITTED, WITHDRAWN, SHORTLISTED, REJECTED, SELECTED |
| Valid transitions | SUBMITTED → WITHDRAWN/SHORTLISTED/REJECTED/SELECTED. |
| Invalid transitions | Apply job chưa PUBLISHED; apply trùng; Expert apply job của chính mình nếu không đúng role; Client tự tạo application. |
| Transaction requirements | applyJobPost tạo application và notification trong cùng transaction business. |
| Rollback requirements | Nếu notification fail không được làm fail application nếu dùng async; nếu sync thì rollback rõ ràng. |
| Notification triggers | Client nhận thông báo có application mới; Expert nhận thông báo selected/rejected. |
| Audit logging requirements | appliedAt, reviewedAt, reviewedBy Client. |


## 6.3 Invitation

| Hạng mục | Quy định |
| --- | --- |
| Lifecycle states | PENDING, ACCEPTED, REJECTED, EXPIRED, CANCELLED |
| Valid transitions | PENDING → ACCEPTED/REJECTED/EXPIRED/CANCELLED. |
| Invalid transitions | Accept invitation không thuộc Expert; accept khi expired; accept nhiều lần; Client accept thay Expert. |
| Transaction requirements | acceptInvitation bắt buộc tạo Project + Contract + ProjectEscrow + ProjectConversation atomically. |
| Rollback requirements | Nếu tạo ProjectEscrow fail thì Project/Contract/Conversation không được tồn tại orphan. |
| Notification triggers | Invitation sent, accepted, rejected, expired. |
| Audit logging requirements | sentBy Client, acceptedBy Expert, respondedAt. |


## 6.4 Project

| Hạng mục | Quy định |
| --- | --- |
| Lifecycle states | PENDING_FUNDING, ACTIVE, IN_REVIEW, REVISION_REQUIRED, COMPLETED, CANCELLED, DISPUTED |
| Valid transitions | PENDING_FUNDING → ACTIVE khi escrow HELD; ACTIVE → IN_REVIEW; IN_REVIEW → COMPLETED/REVISION_REQUIRED/DISPUTED; REVISION_REQUIRED → ACTIVE/IN_REVIEW. |
| Invalid transitions | ACTIVE khi escrow chưa HELD; submit deliverable khi PENDING_FUNDING; review khi chưa COMPLETED; cancel sau release tiền. |
| Transaction requirements | Project state update phải đi qua ProjectLifecycleService, không setStatus trực tiếp trong controller. |
| Rollback requirements | Nếu approve deliverable release tiền fail thì Project không được COMPLETED. |
| Notification triggers | Project created, escrow funded, project active, deliverable submitted, completed, disputed. |
| Audit logging requirements | clientId, expertId, acceptedInvitationId, startedAt, completedAt. |


## 6.5 ProjectEscrow

| Hạng mục | Quy định |
| --- | --- |
| Lifecycle states | PENDING, HELD, RELEASED, REFUNDED, PARTIALLY_RELEASED, DISPUTED |
| Valid transitions | PENDING → HELD; HELD → RELEASED/REFUNDED/DISPUTED; DISPUTED → RELEASED/REFUNDED/PARTIALLY_RELEASED. |
| Invalid transitions | Release trước HELD; refund sau RELEASED; HELD khi payment failed; amount âm; double release. |
| Transaction requirements | fundEscrow, releaseEscrow, refundEscrow phải cùng transaction với PaymentTransaction và Wallet ledger. |
| Rollback requirements | Bất kỳ lỗi update wallet/payment làm rollback toàn bộ escrow state. |
| Notification triggers | Escrow funded, released, refunded, dispute hold. |
| Audit logging requirements | heldAt, releasedAt, refundedAt, paymentTransactionId. |


## 6.6 Deliverable

| Hạng mục | Quy định |
| --- | --- |
| Lifecycle states | DRAFT, SUBMITTED, REVISION_REQUESTED, REVISION_SUBMITTED, APPROVED, REJECTED |
| Valid transitions | DRAFT → SUBMITTED; SUBMITTED → APPROVED/REVISION_REQUESTED; REVISION_REQUESTED → REVISION_SUBMITTED; REVISION_SUBMITTED → APPROVED/REVISION_REQUESTED. |
| Invalid transitions | Submit khi Project không ACTIVE; Client approve deliverable không thuộc project; Expert sửa sau APPROVED. |
| Transaction requirements | approveDeliverable phải release escrow + update wallet + complete project trong cùng transaction. |
| Rollback requirements | Release tiền fail thì Deliverable không APPROVED. |
| Notification triggers | Submitted, revision requested, revised, approved. |
| Audit logging requirements | submittedBy Expert, approvedBy Client, submittedAt, approvedAt. |


## 6.7 ServiceOrder

| Hạng mục | Quy định |
| --- | --- |
| Lifecycle states | CREATED, PAID, IN_PROGRESS, DELIVERED, REVISION_REQUESTED, COMPLETED, CANCELLED, REFUNDED, DISPUTED |
| Valid transitions | CREATED → PAID → IN_PROGRESS → DELIVERED → COMPLETED; DELIVERED → REVISION_REQUESTED; REVISION_REQUESTED → IN_PROGRESS/DELIVERED. |
| Invalid transitions | Order service unpublished; complete khi chưa delivered; deliver bởi user không phải owner Expert; double purchase không idempotent. |
| Transaction requirements | purchaseService tạo ServiceOrder + PaymentTransaction + Invoice atomically. |
| Rollback requirements | Payment fail thì không tạo PAID order/invoice completed. |
| Notification triggers | Order purchased, accepted/in progress, delivered, completed, refunded. |
| Audit logging requirements | clientId, expertId, serviceId, purchasedAt, completedAt. |


## 6.8 ServiceDelivery

| Hạng mục | Quy định |
| --- | --- |
| Lifecycle states | SUBMITTED, REVISION_REQUESTED, ACCEPTED, REJECTED |
| Valid transitions | SUBMITTED → ACCEPTED/REVISION_REQUESTED; REVISION_REQUESTED → SUBMITTED. |
| Invalid transitions | Submit delivery cho order không PAID/IN_PROGRESS; Client accept order của người khác. |
| Transaction requirements | acceptDelivery cập nhật ServiceOrder COMPLETED, tạo ClientService, credit wallet nếu flow yêu cầu. |
| Rollback requirements | Nếu tạo ClientService fail thì ServiceOrder không COMPLETED. |
| Notification triggers | Delivery submitted, revision requested, accepted. |
| Audit logging requirements | deliveredBy, acceptedBy, deliveredAt. |


## 6.9 PaymentTransaction

| Hạng mục | Quy định |
| --- | --- |
| Lifecycle states | INITIATED, SUCCESS, FAILED, CANCELLED, REFUNDED |
| Valid transitions | INITIATED → SUCCESS/FAILED/CANCELLED; SUCCESS → REFUNDED nếu refund hợp lệ. |
| Invalid transitions | SUCCESS hai lần cùng idempotencyKey; amount âm; transaction không link owner/order/project. |
| Transaction requirements | Mọi money movement tạo PaymentTransaction trước hoặc trong cùng transaction ledger. |
| Rollback requirements | Nếu ledger update fail thì transaction không SUCCESS. |
| Notification triggers | Payment success/failed/refunded. |
| Audit logging requirements | transactionCode, payerId, payeeId, referenceType, referenceId. |


## 6.10 WithdrawalRequest

| Hạng mục | Quy định |
| --- | --- |
| Lifecycle states | PENDING, APPROVED, REJECTED, PAID, CANCELLED |
| Valid transitions | PENDING → APPROVED/REJECTED/CANCELLED; APPROVED → PAID. |
| Invalid transitions | Request vượt available balance; Expert hủy sau APPROVED; Admin mark PAID khi chưa APPROVED. |
| Transaction requirements | createWithdrawal reserve available balance nếu ERD hỗ trợ; approve/paid update ledger rõ ràng. |
| Rollback requirements | Nếu ledger fail thì withdrawal không đổi status. |
| Notification triggers | Request created, approved, rejected, paid. |
| Audit logging requirements | requestedBy Expert, approvedBy Admin, paidAt. |


## 6.11 Dispute

| Hạng mục | Quy định |
| --- | --- |
| Lifecycle states | OPEN, UNDER_REVIEW, RESOLVED_RELEASE, RESOLVED_REFUND, RESOLVED_PARTIAL, CANCELLED |
| Valid transitions | OPEN → UNDER_REVIEW; UNDER_REVIEW → resolved states; OPEN → CANCELLED nếu người mở hủy trước xử lý. |
| Invalid transitions | Open dispute cho project/order đã completed lâu quá hạn; user không liên quan mở dispute; resolve không bởi Admin. |
| Transaction requirements | resolveDispute phải update Project/Order + Escrow/Wallet/PaymentTransaction atomically. |
| Rollback requirements | Nếu refund/release fail thì Dispute không RESOLVED. |
| Notification triggers | Dispute opened, admin reviewing, resolved. |
| Audit logging requirements | openedBy, assignedAdmin, resolvedBy, resolvedAt, resolutionNote. |


# 7. CHIẾN LƯỢC FREEZE DATABASE VÀ API CONTRACT

| Mốc freeze | Tuần | Phạm vi freeze | Rủi ro nếu đổi muộn | Quy trình khi bắt buộc đổi |
| --- | --- | --- | --- | --- |
| ERD Freeze lần 1 | Cuối Week 3 | User, Profile, Wallet, JobPost, Application, ExpertService baseline. | Merge conflict entity, migration lệch máy dev, mapper fail. | Tạo migration note, PR có reviewer owner, update diagram. |
| Enum Freeze lần 1 | Cuối Week 4 | Role, UserStatus, JobPostStatus, ApplicationStatus, InvitationStatus, ServiceStatus. | Frontend hardcode status text bị lệch; test state fail. | Chỉ thêm enum nếu có mapping response rõ ràng. |
| Relationship Freeze custom project | Giữa Week 5 | Invitation → Project → Contract → Escrow → Conversation. | Orphan data, acceptInvitation rollback khó kiểm soát. | Tech Lead review trước khi merge. |
| Payment schema Freeze | Cuối Week 5 | Wallet, PaymentTransaction, ProjectEscrow, Invoice. | Lệch tiền, mất audit, migration phá dữ liệu demo. | Không đổi trực tiếp DB prod/demo; viết migration versioned. |
| Deliverable/Order Freeze | Cuối Week 6 | Deliverable, ServiceOrder, ServiceDelivery, ClientService, ClientProjectService. | Flow complete/review bị sai. | Update E2E scripts ngay khi đổi. |
| Frontend Contract Freeze lần 1 | Cuối Week 7 | Response DTO cho flow chính. | Frontend integration vỡ hàng loạt. | Tạo changelog, thông báo frontend, giữ field cũ nếu có thể. |
| Backend Feature Freeze | Cuối Week 8 | Không thêm module lớn mới. | Week 9 biến thành tuần code feature, trễ demo. | Chỉ cho P0/P1 bug và security fix. |
| Release Candidate Freeze | Giữa Week 10 | Code demo/deployment. | Demo không ổn định. | Chỉ hotfix blocker có approval. |


## 7.1 Quy tắc migration

- Không sửa tay database local rồi quên cập nhật schema/migration note.
- Không đổi kiểu dữ liệu money tùy tiện; amount nên dùng BigDecimal ở code và DECIMAL ở DB.
- Không xóa column đang được frontend hoặc test dùng; deprecate trước nếu cần.
- Mọi enum lưu DB phải thống nhất kiểu lưu STRING nếu team chọn dễ debug; không đổi giữa ORDINAL và STRING giữa chừng.
- Index cần thêm trước Week 9 cho các query nhiều dữ liệu: owner_id, status, project_id, conversation_id, created_at, reference_id.

# 8. QUY TẮC KIẾN TRÚC BACKEND BẮT BUỘC

## 8.1 Package structure chuẩn

```text
com.example.aitasker
  ├── config
  ├── security
  ├── common
  │   ├── response
  │   ├── exception
  │   ├── validation
  │   └── pagination
  ├── entity
  ├── enums
  ├── repository
  ├── mapper
  ├── dto
  │   ├── request
  │   └── response
  ├── service
  │   ├── impl
  │   └── workflow
  └── controller
```

| Khu vực | Rule bắt buộc |
| --- | --- |
| Naming convention | Entity dùng singular noun: Project, ProjectEscrow. DTO dùng hậu tố Request/Response. Service interface nếu dùng thì tên rõ nghiệp vụ, không đặt I chung chung nếu team không thống nhất. |
| DTO structure | Request chỉ nhận field client được phép gửi. Response là contract cho frontend, không trả entity trực tiếp. |
| Mapper rules | MapStruct chỉ map field đơn giản. Relationship owner/user/project phải gán trong service sau khi validate. |
| Repository rules | Repository chỉ query data, không chứa business decision. Query detail phải hỗ trợ owner/participant validation. |
| Service rules | Service xử lý validation, ownership, state transition, transaction. Controller không setStatus trực tiếp. |
| Workflow service | Flow nhiều entity như acceptInvitation, fundEscrow, approveDeliverable, purchaseService, resolveDispute phải đặt ở workflow/service có @Transactional. |
| Transaction boundary | Transaction bắt đầu ở service public method. Không mở transaction ở controller. Không gọi external IO dài trong transaction nếu không cần. |
| Validation layer | Bean Validation cho input cơ bản; business validation trong service; DB constraint cho unique/not null quan trọng. |
| Exception handling | Không throw RuntimeException thô ở flow chính. Dùng BusinessException/ErrorCode để frontend nhận lỗi ổn định. |
| Pagination | Mọi listing nhiều dữ liệu phải dùng page,size,sort. Không trả list không giới hạn cho message/payment/project. |
| Search/filter | Filter phải rõ field được hỗ trợ. Không build query string nguy hiểm hoặc filter tùy tiện chưa index. |
| Soft delete | Chỉ soft delete với dữ liệu nghiệp vụ cần audit. PaymentTransaction/Invoice không xóa cứng. |
| Timestamp | createdAt/updatedAt dùng Auditing. Business time như paidAt, releasedAt, completedAt set trong service. |
| Enum handling | Không hardcode string status rải rác. Dùng enum và transition matrix. |
| API response wrapper | Tất cả response theo APIResponse<T>. Lỗi validation theo format chung. |
| Role validation | Endpoint security check role trước, service check owner/participant sau. |
| Ownership validation | Không tin userId trong request. Lấy user hiện tại từ SecurityContext. |


# 9. QUY ƯỚC API HOÀN CHỈNH

## 9.1 REST naming convention

| Quy ước | Chuẩn dùng trong project |
| --- | --- |
| Base URL | /api |
| Resource naming | Danh từ số nhiều: /job-posts, /projects, /service-orders. |
| Action endpoint | Dùng action cho state transition: /publish, /accept, /approve, /request-revision. |
| Pagination | page bắt đầu từ 0 hoặc 1 phải thống nhất; đề xuất page=0 theo Spring Pageable. |
| Filtering | Dùng query param: status, keyword, categoryId, minBudget, maxBudget, fromDate, toDate. |
| Sorting | sort=createdAt,desc hoặc sort=budget,asc. |
| Response success | APIResponse gồm status, message, data, timestamp. |
| Response error | APIResponse hoặc ErrorResponse gồm status, code, message, errors, timestamp, path. |
| Status update | Không dùng PUT toàn entity để đổi status; dùng endpoint transition riêng. |
| Security | Bearer token cho endpoint protected; public endpoint phải whitelist rõ trong SecurityConfig. |


## 9.2 Mẫu response chuẩn

```json
{
  "status": 200,
  "message": "Success",
  "data": {},
  "timestamp": "2026-05-27T10:00:00Z"
}
```

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu request không hợp lệ",
  "errors": {
    "budget": "budget must be greater than 0"
  },
  "timestamp": "2026-05-27T10:00:00Z",
  "path": "/api/job-posts"
}
```

## 9.3 API nhóm Auth/User/Profile

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| POST | /api/auth/register/client | Client đăng ký tài khoản và ClientProfile. |
| POST | /api/auth/register/expert | Expert đăng ký tài khoản và ExpertProfile. |
| POST | /api/auth/login | Đăng nhập và nhận access token. |
| GET | /api/users/me | Lấy thông tin user hiện tại. |
| PUT | /api/client-profiles/me | Client cập nhật profile của chính mình. |
| PUT | /api/expert-profiles/me | Expert cập nhật profile của chính mình. |


## 9.3 API nhóm JobPost/Application/Proposal

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| POST | /api/job-posts | Client tạo JobPost ở trạng thái DRAFT. |
| PUT | /api/job-posts/{id} | Client owner cập nhật JobPost khi chưa CLOSED. |
| POST | /api/job-posts/{id}/publish | Client publish JobPost. |
| GET | /api/job-posts | Danh sách public có pagination/filter. |
| POST | /api/job-posts/{id}/applications | Expert apply vào JobPost. |
| GET | /api/job-posts/{id}/applications | Client owner xem danh sách application. |
| POST | /api/applications/{id}/shortlist | Client shortlist ExpertApplication. |
| POST | /api/applications/{id}/reject | Client reject ExpertApplication. |


## 9.3 API nhóm Invitation/Project/Contract

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| POST | /api/applications/{id}/invitations | Client gửi ProjectInvitation cho Expert. |
| POST | /api/invitations/{id}/accept | Expert accept invitation; hệ thống tạo Project/Contract/Escrow/Conversation. |
| POST | /api/invitations/{id}/reject | Expert từ chối invitation. |
| GET | /api/projects | Client/Expert xem project của mình. |
| GET | /api/projects/{id} | Participant xem detail project. |
| GET | /api/projects/{id}/contract | Participant xem contract snapshot. |


## 9.3 API nhóm Escrow/Deliverable/Dispute

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| POST | /api/projects/{id}/escrow/fund | Client fund escrow cho Project. |
| GET | /api/projects/{id}/escrow | Participant/Admin xem escrow state. |
| POST | /api/projects/{id}/deliverables | Expert submit deliverable. |
| POST | /api/deliverables/{id}/request-revision | Client yêu cầu revision. |
| POST | /api/deliverables/{id}/approve | Client approve; hệ thống release escrow. |
| POST | /api/projects/{id}/disputes | Participant mở dispute. |
| POST | /api/disputes/{id}/resolve | Admin resolve dispute release/refund/partial. |


## 9.3 API nhóm ExpertService/ServiceOrder

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| POST | /api/expert-services | Expert tạo service ở DRAFT. |
| POST | /api/expert-services/{id}/publish | Expert publish service. |
| GET | /api/expert-services | Public listing service. |
| POST | /api/expert-services/{id}/orders | Client purchase service. |
| GET | /api/service-orders | Client/Expert xem order của mình. |
| POST | /api/service-orders/{id}/deliveries | Expert submit ServiceDelivery. |
| POST | /api/service-deliveries/{id}/accept | Client accept delivery; tạo ClientService. |


## 9.3 API nhóm Wallet/Payment/Invoice/Withdrawal

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| GET | /api/wallets/me | User xem ví của mình. |
| GET | /api/wallets/me/transactions | User xem ledger với pagination. |
| GET | /api/invoices/{id} | Owner/Admin xem invoice. |
| POST | /api/withdrawals | Expert tạo WithdrawalRequest. |
| GET | /api/withdrawals/me | Expert xem withdrawal của mình. |
| POST | /api/admin/withdrawals/{id}/approve | Admin approve withdrawal. |
| POST | /api/admin/withdrawals/{id}/reject | Admin reject withdrawal. |
| POST | /api/admin/withdrawals/{id}/mark-paid | Admin đánh dấu đã thanh toán ngoài hệ thống. |


## 9.3 API nhóm Notification/Review/Admin

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| GET | /api/notifications | User xem notification. |
| POST | /api/notifications/{id}/read | User mark read notification. |
| POST | /api/projects/{id}/reviews | Client review sau Project COMPLETED. |
| POST | /api/service-orders/{id}/reviews | Client review sau ServiceOrder COMPLETED. |
| GET | /api/admin/users | Admin quản lý user. |
| PATCH | /api/admin/users/{id}/status | Admin đổi UserStatus. |
| GET | /api/admin/projects | Admin inspect projects. |
| GET | /api/admin/payments | Admin inspect payments. |
| GET | /api/admin/disputes | Admin xử lý disputes. |


# 10. CHIẾN LƯỢC GIT VÀ BRANCHING

| Branch | Mục đích | Rule |
| --- | --- | --- |
| main | Code ổn định để demo/deploy. | Không direct push. Chỉ merge từ release/hotfix đã review. |
| develop | Tích hợp code hằng tuần. | Mọi feature merge vào develop qua Pull Request. |
| feature/backend/<module> | Code module hoặc API cụ thể. | Tên branch rõ module, scope nhỏ, rebase develop trước PR. |
| release/week-<n> | Chốt code cuối tuần hoặc release candidate. | Chỉ nhận bugfix, không nhận feature mới. |
| hotfix/<issue> | Fix lỗi blocker trên main/release. | PR bắt buộc, mô tả root cause và test đã chạy. |


## 10.1 Quy ước đặt tên branch

```text
feature/backend/auth-jwt
feature/backend/jobpost-api
feature/backend/project-invitation-flow
feature/backend/escrow-funding
feature/backend/deliverable-approval
feature/backend/service-order-purchase
feature/backend/wallet-withdrawal
fix/backend/project-ownership-check
hotfix/payment-double-release
release/week-7-core-backend
```

## 10.2 Quy ước commit

| Prefix | Khi dùng | Ví dụ |
| --- | --- | --- |
| feat | Thêm feature/API/entity mới. | feat(project): create accept invitation workflow |
| fix | Sửa bug. | fix(escrow): prevent double release |
| refactor | Đổi cấu trúc code không đổi behavior. | refactor(security): extract ownership validator |
| test | Thêm hoặc sửa test. | test(wallet): add rollback test for release |
| docs | Cập nhật tài liệu/API contract. | docs(api): update service order endpoints |
| chore | Cấu hình/build/cleanup. | chore(git): update PR template |


## 10.3 Quy tắc PR

- Mỗi PR phải có mô tả: module, API thay đổi, entity thay đổi, test đã chạy, rủi ro.
- Không merge PR làm đổi entity shared nếu owner chưa review.
- PR có transaction money flow phải có rollback test hoặc mô tả test manual rõ ràng.
- Ưu tiên squash merge cho feature branch nhỏ; merge commit cho release branch nếu cần giữ lịch sử.
- Rebase develop trước khi mở PR để giảm conflict.

# 11. CHIẾN LƯỢC TÍCH HỢP FRONTEND

| Tuần | API frontend có thể dùng | API chưa ổn định | Ghi chú integration |
| --- | --- | --- | --- |
| Week 3 | Auth, register, login, profile, job listing baseline. | Wallet/payment chỉ read-only hoặc chưa dùng. | Frontend có thể dựng layout và auth guard. |
| Week 4 | JobPost, Application, Invitation inbox, ExpertService listing. | Project detail, escrow, deliverable. | Frontend dùng mock cho workspace project. |
| Week 5 | Project detail, contract, conversation, fund escrow. | Deliverable approval/release chưa ổn định. | Bắt đầu integration custom project thật. |
| Week 6 | Deliverable, Dispute, ServiceOrder, ServiceDelivery. | Admin resolve nâng cao, withdrawal. | Frontend nối flow end-to-end. |
| Week 7 | Core APIs freeze lần 1. | Admin analytics, một số filter phụ. | Không đổi response field nếu không báo trước. |
| Week 8 | Full backend gần như đủ. | Chỉ edge cases. | Frontend bỏ mock ở flow chính. |
| Week 9 | API freeze final. | Không có API mới. | Chạy E2E frontend/backend. |
| Week 10 | Release candidate. | Không đổi contract. | Chuẩn bị demo script. |


## 11.1 Chiến lược contract-first

- Backend tạo Postman collection/OpenAPI note trước khi frontend code màn hình chính.
- Mỗi endpoint có sample success, validation error, unauthorized, forbidden, invalid state.
- Response DTO phải có field ổn định: id, status, title/name, owner summary, createdAt, updatedAt.
- Không trả entity nested quá sâu; dùng summary response để tránh vòng lặp JSON.
- Khi đổi API, cập nhật changelog và báo frontend trong ngày, không để cuối tuần mới báo.

## 11.2 Danh sách API chưa ổn định cần tránh dùng sớm

| API | Lý do chưa ổn định | Khi nào dùng được |
| --- | --- | --- |
| Dispute resolve | Phụ thuộc escrow/wallet/refund logic. | Sau Week 8. |
| Withdrawal approve/paid | Phụ thuộc wallet reserve/ledger. | Sau Week 8. |
| Admin financial dashboard | Phụ thuộc PaymentTransaction/Invoice đủ dữ liệu. | Cuối Week 8. |
| Advanced search/filter | Phụ thuộc index và query optimization. | Week 8-9. |
| Notification event payload | Có thể thay đổi theo flow. | Freeze Week 7. |


# 12. CHIẾN LƯỢC TESTING HOÀN CHỈNH

| Loại test | Phạm vi | Ví dụ bắt buộc |
| --- | --- | --- |
| Unit test | Service logic, mapper, state transition. | Invalid Project transition, wallet balance check, duplicate apply. |
| Integration test | Repository + service + DB transaction. | acceptInvitation tạo đủ Project/Contract/Escrow/Conversation. |
| Security test | Role và endpoint protection. | Guest gọi protected API bị 401; Expert sửa JobPost của Client bị 403. |
| Ownership test | Owner/participant validation. | Client A không xem Project của Client B. |
| Rollback test | Transaction nhiều entity. | approveDeliverable fail wallet update thì Deliverable không APPROVED. |
| Escrow consistency test | ProjectEscrow + PaymentTransaction + Wallet. | fundEscrow success tạo HELD; double fund bị chặn. |
| State transition test | Status lifecycle. | Deliverable submit chỉ khi Project ACTIVE. |
| E2E Postman/Newman | Flow thật từ đầu đến cuối. | Custom project full flow và ready-made service full flow. |


## 12.1 Kịch bản test mẫu

| Mã test | Flow | Kịch bản | Kết quả mong đợi |
| --- | --- | --- | --- |
| AUTH-001 | Login | Login đúng email/password. | Trả access token và user summary. |
| AUTH-002 | RBAC | Expert gọi API tạo JobPost. | 403 FORBIDDEN. |
| JOB-001 | JobPost | Client tạo và publish JobPost hợp lệ. | Status chuyển PUBLISHED. |
| APP-001 | Application | Expert apply JobPost PUBLISHED. | Application SUBMITTED, notification tạo. |
| APP-002 | Application | Expert apply trùng JobPost. | 409 DUPLICATE_APPLICATION. |
| INV-001 | Invitation | Client send invitation từ selected application. | Invitation PENDING. |
| INV-002 | Invitation | Expert accept invitation hợp lệ. | Tạo Project PENDING_FUNDING, Contract, Escrow PENDING, Conversation. |
| ESC-001 | Escrow | Client fund escrow đủ tiền. | Escrow HELD, Project ACTIVE, PaymentTransaction SUCCESS. |
| ESC-002 | Escrow | Fund escrow hai lần. | Request thứ hai bị chặn, không double debit. |
| DEL-001 | Deliverable | Expert submit deliverable khi Project ACTIVE. | Deliverable SUBMITTED, Project IN_REVIEW. |
| DEL-002 | Deliverable | Client approve deliverable. | Deliverable APPROVED, Escrow RELEASED, Project COMPLETED, Wallet Expert tăng. |
| DEL-003 | Rollback | Lỗi khi update wallet trong approve. | Không release escrow, project không completed. |
| SER-001 | ServiceOrder | Client purchase ExpertService PUBLISHED. | ServiceOrder PAID/IN_PROGRESS, PaymentTransaction SUCCESS, Invoice tạo. |
| SER-002 | ServiceOrder | Purchase service UNPUBLISHED. | 400 INVALID_SERVICE_STATUS. |
| REV-001 | Review | Client review Project COMPLETED. | Review tạo thành công. |
| REV-002 | Review | Review trước completed. | 400 REVIEW_NOT_ALLOWED. |
| WIT-001 | Withdrawal | Expert tạo withdrawal hợp lệ. | Withdrawal PENDING, amount được validate với available balance. |
| DIS-001 | Dispute | Client mở dispute project đang IN_REVIEW. | Dispute OPEN, Project DISPUTED hoặc flag disputed. |
| OWN-001 | Ownership | Client A xem wallet Client B. | 403 FORBIDDEN_OWNER. |
| MSG-001 | Conversation | User không phải participant gửi message. | 403 CONVERSATION_ACCESS_DENIED. |


# 13. QUẢN LÝ RỦI RO HOÀN CHỈNH

| Rủi ro | Mức | Tác động | Biện pháp giảm rủi ro |
| --- | --- | --- | --- |
| Escrow consistency | P0 | Lệch trạng thái ProjectEscrow với PaymentTransaction/Wallet. | Bắt buộc @Transactional ở fund/release/refund; test rollback; unique transactionCode; không update tiền ngoài WalletService. |
| Double payment | P0 | Client bấm fund/purchase nhiều lần hoặc retry request. | Check status trước khi xử lý; idempotencyKey nếu có; lock theo project/order; unique payment reference. |
| Wallet inconsistency | P0 | Balance âm, release hai lần, withdrawal vượt số dư. | Ledger-based update, optimistic/pessimistic lock, test concurrent request. |
| State transition sai | P0 | Project ACTIVE khi chưa escrow HELD hoặc review trước completed. | StateTransitionService, enum matrix, unit test invalid transition. |
| Ownership bypass | P0 | User truy cập project/order/conversation của người khác. | Không chỉ check role; mọi query detail phải check participant/owner. |
| Circular dependency | P1 | ProjectService gọi EscrowService gọi ProjectService vòng lặp. | Tách orchestration service cho transaction lớn: ProjectWorkflowService, PaymentWorkflowService. |
| Frontend/backend mismatch | P1 | DTO đổi muộn làm frontend vỡ. | Freeze response Week 7, API changelog, versioned response khi đổi bắt buộc. |
| Enum explosion | P1 | Nhiều enum trùng nghĩa giữa Project/Order/Payment. | Review enum trước freeze; không thêm enum mới nếu có thể map bằng status hiện có. |
| Conversation scaling | P2 | Message list chậm, N+1 participant. | Pagination bắt buộc, index conversation_id + created_at, không trả full message trong project detail. |
| Merge conflict | P2 | 3 dev sửa cùng entity/DTO. | Ownership matrix; shared entity chỉ sửa qua PR có reviewer owner. |


## 13.1 Quy tắc xử lý bug theo mức độ

| Mức | Định nghĩa | Thời gian xử lý |
| --- | --- | --- |
| P0 | Lỗi tiền, security leak, data corruption, flow core không chạy. | Fix ngay trong ngày, chặn merge/release. |
| P1 | Flow chính bị lỗi nhưng có workaround hoặc không mất tiền. | Fix trong 24-48 giờ. |
| P2 | UI/API phụ, filter, message, text response, minor validation. | Gom vào bug batch trong tuần. |
| P3 | Polish, cleanup, naming, log, documentation nhỏ. | Fix khi có buffer. |


# 14. CHIẾN LƯỢC PERFORMANCE VÀ SECURITY

## 14.1 Chiến lược security

| Khu vực | Rule triển khai |
| --- | --- |
| JWT flow | Login trả access token; filter parse token; SecurityContext chứa userId/role; không lấy userId từ request nếu không cần. |
| RBAC | Guest chỉ xem public endpoints. Client tạo JobPost/purchase/review. Expert apply/deliver/create service. Admin inspect/resolve/manage. |
| Endpoint protection | Whitelist public endpoints rõ ràng; mọi endpoint khác authenticated. |
| Ownership validation | Sau RBAC, service phải check owner/participant/resource access. |
| Proposal protection | Proposal chi tiết chỉ Client owner của JobPost, Expert owner proposal, hoặc Admin được xem. |
| Conversation validation | Chỉ participant của DirectConversation/ProjectConversation được đọc/gửi message. |
| Escrow permission | Chỉ Client owner fund escrow; chỉ flow approve/dispute resolve được release/refund. |
| Admin permission | Admin action phải audit; không dùng admin endpoint để bypass money workflow. |
| Validation | Reject amount âm, deadline quá khứ, status invalid, file metadata sai, rating ngoài range. |
| Sensitive data | Không log password/token/payment secret; không trả passwordHash trong response. |


## 14.2 Chiến lược performance

| Khu vực | Rule triển khai |
| --- | --- |
| Pagination | Bắt buộc cho JobPost, Project, ServiceOrder, Message, PaymentTransaction, Notification, Admin list. |
| Lazy loading | Không serialize entity trực tiếp. DTO response load đúng dữ liệu cần thiết. |
| N+1 prevention | Dùng fetch join/entity graph/query projection cho listing cần owner summary. |
| Indexing | Index status, owner_id, expert_id, client_id, project_id, order_id, conversation_id, created_at. |
| Query optimization | Không trả full conversation trong project detail; message lấy riêng theo page. |
| Caching | Chỉ cân nhắc cache public service/category listing nếu cần; không cache wallet/payment sensitive data tùy tiện. |
| File metadata | Deliverable chỉ lưu metadata/link; không nhét file binary vào DB. |
| Admin dashboard | Dashboard counts dùng query tổng hợp, không load toàn bộ entity rồi count trong Java. |


# 15. CHIẾN LƯỢC ƯU TIÊN MVP KHI DEADLINE CĂNG

Lưu ý: dự án yêu cầu làm đầy đủ theo SRS. Phần dưới chỉ là chiến lược bảo vệ delivery nếu timeline bị trễ nặng, không phải lý do để tự ý bỏ business flow.

| Mức ưu tiên | Bắt buộc giữ / Có thể giảm | Module |
| --- | --- | --- |
| BẮT BUỘC GIỮ | Bắt buộc giữ | Auth, RBAC, User/Profile, JobPost, Application, Invitation, Project, Contract, ProjectEscrow, Deliverable, ExpertService, ServiceOrder, ServiceDelivery, Wallet, PaymentTransaction, Invoice, Review cơ bản. |
| NÊN CÓ | Nên giữ | Notification đầy đủ, Admin inspection, Withdrawal, Dispute cơ bản, Conversation pagination, search/filter cơ bản. |
| CÓ THỂ ĐỂ SAU | Có thể delay nếu thật sự cần | Advanced analytics admin, complex partial dispute resolution, advanced recommendation, advanced notification preferences, export report, complex invoice PDF. |


## 15.1 Module không được cắt

- ProjectEscrow lifecycle.
- Wallet consistency.
- PaymentTransaction audit.
- Project/ServiceOrder state transition.
- Ownership validation.
- Review permission.
- Deliverable/ServiceDelivery completion flow.

## 15.2 Module có thể giảm scope nếu trễ

- Admin dashboard chỉ cần list/inspect/resolve core, chưa cần chart nâng cao.
- Notification có thể synchronous và đơn giản, chưa cần realtime websocket.
- Dispute có thể release hoặc refund full trước, partial resolution để sau nếu không kịp.
- Search/filter nâng cao có thể giảm còn keyword/status/category.

# 16. DEFINITION OF DONE THEO TỪNG TUẦN

## Week 3 DONE checklist

- [ ] JWT login/register chạy ổn định

- [ ] APIResponse và GlobalException dùng chung

- [ ] User/Profile/Wallet baseline chạy được

- [ ] JobPost/Application skeleton có entity/repository/service/controller

- [ ] Validation error trả format chuẩn

- [ ] Security role test cơ bản pass

- [ ] Develop branch build pass



## Week 4 DONE checklist

- [ ] JobPost publish/close/cancel chạy

- [ ] ExpertApplication apply/withdraw/reject/shortlist chạy

- [ ] ProjectInvitation send/accept/reject skeleton đúng rule

- [ ] ExpertService create/publish chạy

- [ ] Ownership helper được áp dụng

- [ ] Notification event cơ bản được gọi

- [ ] Postman collection cập nhật



## Week 5 DONE checklist

- [ ] acceptInvitation tạo Project/Contract/Escrow/Conversation atomically

- [ ] Project PENDING_FUNDING sau accept

- [ ] fundEscrow chuyển Escrow HELD và Project ACTIVE

- [ ] PaymentTransaction SUCCESS tạo đúng

- [ ] Wallet không âm

- [ ] Rollback fundEscrow được test

- [ ] Frontend có thể gọi project detail/fund escrow



## Week 6 DONE checklist

- [ ] Deliverable submit/revision/approve chạy

- [ ] approveDeliverable release escrow và update wallet

- [ ] ServiceOrder purchase tạo payment/invoice/order

- [ ] ServiceDelivery accept tạo ClientService

- [ ] Review permission bắt đầu hoạt động

- [ ] Dispute basic open chạy

- [ ] E2E custom/service flow chạy được



## Week 7 DONE checklist

- [ ] Custom project end-to-end pass

- [ ] Ready-made service end-to-end pass

- [ ] Notification đủ event core

- [ ] Admin inspect core data

- [ ] API contract freeze lần 1

- [ ] Security matrix chạy qua tất cả module chính

- [ ] Không còn hardcode status nguy hiểm



## Week 8 DONE checklist

- [ ] Backend feature complete

- [ ] Withdrawal flow chạy

- [ ] Dispute resolve chạy

- [ ] Admin actions có audit

- [ ] Pagination/filter cho listing chính

- [ ] Không còn RuntimeException thô ở flow chính

- [ ] Regression test pass



## Week 9 DONE checklist

- [ ] Không thêm feature mới

- [ ] E2E frontend/backend pass

- [ ] Rollback tests pass

- [ ] Security/ownership tests pass

- [ ] Performance/N+1 check xong

- [ ] P0/P1 bug đã fix

- [ ] API freeze final



## Week 10 DONE checklist

- [ ] Release candidate ổn định

- [ ] Demo data seed xong

- [ ] Deployment config sẵn sàng

- [ ] Script demo custom project sẵn sàng

- [ ] Script demo service order/payment sẵn sàng

- [ ] Defense notes cho security/payment/lifecycle sẵn sàng

- [ ] Không merge refactor lớn



# PHỤ LỤC A — BACKLOG MODULE DÙNG ĐỂ GIAO TASK TRỰC TIẾP

| Module | Backlog task cụ thể |
| --- | --- |
| Auth | Tạo AuthController login/register; AuthenticationService; JwtProvider; JwtFilter; SecurityFilterChain; PasswordEncoder; AuthRequest/AuthResponse; test invalid credential/token. |
| User/Profile | Tạo UserService getMe/update status; ClientProfileService; ExpertProfileService; mapper; owner update; admin inspect. |
| JobPost | create/update/publish/close/cancel/search/detail; validate budget/deadline/category; owner check; status transition test. |
| ExpertApplication | apply/withdraw/shortlist/reject/select; duplicate apply check; Expert owner check; Client job owner check. |
| Proposal | create/update proposal content; visibility rule; unlock/payment nếu SRS có; không leak proposal private content. |
| Invitation | send/accept/reject/expire; accept transaction; duplicate accept prevention; notification. |
| Project | list/detail/timeline/status; pending funding to active; participant validation; contract snapshot. |
| Escrow | fund/hold/release/refund/dispute; amount validation; PaymentTransaction link; Wallet update; rollback test. |
| Conversation/Message | create direct/project conversation; send/list message; participant validation; pagination; index. |
| Deliverable | submit/request revision/submit revision/approve; file metadata; project state validation; escrow release trigger. |
| ExpertService | create/update/publish/unpublish/list/detail; owner validation; package/price validation. |
| ServiceOrder | purchase/list/detail/cancel/refund if allowed; PaymentTransaction + Invoice; order state transition. |
| ServiceDelivery | submit/revision/accept; creates ClientService; review permission unlock. |
| Wallet/Payment | wallet detail; ledger; debit/credit/hold/release/refund; transaction code; consistency tests. |
| Invoice | generate invoice for payment; owner/admin access; invoice status; number uniqueness. |
| Review | create project review/service review; one review per completed item; rating validation; expert average rating update if needed. |
| Notification | create event notification; list unread/read; mark read; event names standardized. |
| Withdrawal | request/approve/reject/mark paid; balance validation; admin audit; notification. |
| Dispute | open/comment/review/resolve; refund/release integration; admin permission; audit resolution. |


# PHỤ LỤC B — MA TRẬN ROLE VÀ QUYỀN TRUY CẬP

| Resource | Guest | Client | Expert | Admin |
| --- | --- | --- | --- | --- |
| Public JobPost | Xem | Xem/Tạo/Sửa của mình | Xem/Apply | Xem tất cả |
| Application | Không | Xem application vào job của mình | Tạo/Xem của mình | Xem tất cả |
| Invitation | Không | Gửi/Hủy của mình | Accept/Reject của mình | Xem tất cả |
| Project | Không | Xem project mình là client | Xem project mình là expert | Xem tất cả |
| Escrow | Không | Fund escrow project của mình | Xem trạng thái liên quan | Inspect/resolve qua dispute |
| Deliverable | Không | Review/approve/revision project của mình | Submit project của mình | Inspect |
| ExpertService | Xem public | Xem/Purchase | Tạo/Sửa service của mình | Inspect |
| ServiceOrder | Không | Purchase/Xem order của mình | Deliver order của mình | Inspect |
| Wallet | Không | Xem ví của mình | Xem ví của mình | Inspect theo quyền admin |
| Withdrawal | Không | Không | Tạo/Xem withdrawal của mình | Approve/Reject/Mark paid |
| Dispute | Không | Mở dispute liên quan | Mở dispute liên quan | Resolve |
| Notification | Không | Xem của mình | Xem của mình | Không cần trừ admin system view |

