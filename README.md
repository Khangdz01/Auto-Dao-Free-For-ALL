# AutoDao / AutoMine Client — Fabric 1.21.11

> **Phiên bản:** 1.0.0 · **Target:** Minecraft Fabric 1.21.11 (Tương thích Fabric 1.21.9 – 1.21.11)  
> **Nền tảng:** Java 21 · Gradle / Fabric Loom · Yarn Mappings `1.21.11+build.6`  
> **Tình trạng:** Deobfuscated 100% · Offline Core Rebuilt · Feature Enhanced · Built-in Freecam · Meteor Freecam Compatible

---

## 📑 Mục Lục
1. [🌟 Giới thiệu về Client / Addon](#-giới-thiệu-về-client--addon)
2. [🎥 Module Freecam Riêng Biệt & Khắc Phục Lỗi Freecam Meteor](#-module-freecam-riêng-biệt--khắc-phục-lỗi-freecam-meteor)
3. [📦 Hướng Dẫn Cài Đặt Từng Bước (Ai Cũng Làm Được)](#-hướng-dẫn-cài-đặt-từng-bước-ai-cũng-làm-được)
4. [📖 Hướng Dẫn Sử Dụng Cầm Tay Chỉ Việc (Từ A đến Z)](#-hướng-dẫn-sử-dụng-cầm-tay-chỉ-việc-từ-a-đến-z)
5. [🎮 Phím Tắt & Toàn Bộ Lệnh Chat](#-phím-tắt--toàn-bộ-lệnh-chat)
6. [📡 Hướng Dẫn Cài Đặt Báo Cáo Discord Webhook](#-hướng-dẫn-cài-đặt-báo-cáo-discord-webhook)
7. [🏆 Nhật ký Hoàn thiện & Các Đột phá Kỹ thuật (Engineering Breakthroughs)](#-nhật-ký-hoàn-thiện--các-đột-phá-kỹ-thuật-engineering-breakthroughs)
8. [⚡ So sánh Trước và Sau (Before & After)](#-so-sánh-trước-và-sau-before--after)
9. [🛠 Tính Năng Chi Tiết (Features Breakdown)](#-tính-năng-chi-tiết-features-breakdown)
10. [💻 Yêu cầu hệ thống & Hướng dẫn Tự Biên Dịch (Build from Source)](#-yêu-cầu-hệ-thống--hướng-dẫn-tự-biên-dịch-build-from-source)
11. [❓ Câu Hỏi Thường Gặp & Xử Lý Sự Cố (FAQ)](#-câu-hỏi-thường-gặp--xử-lý-sự-cố-faq)
12. [⚠️ Lưu Ý Quan Trọng & Lời Nhắn Gửi Chân Thành](#️-lưu-ý-quan-trọng--lời-nhắn-gửi-chân-thành)

---

## 🌟 Giới thiệu về Client / Addon

**AutoDao (AutoMine)** là một client mod tự động hóa đào block thông minh bậc nhất dành cho Minecraft Fabric (1.21.11). Mod được thiết kế chuyên biệt cho việc cày cuốc tài nguyên (Anarchy, Survival, Prison, Skyblock, SMP):

- **Tự động quét và đào khối hộp 3D (Cuboid Selection):** Đào từng lớp (layer-by-layer) từ trên xuống dưới hoặc quét ngang (sweep).
- **Tự động lưu vùng chọn 3D (Auto-Save Box Area):** Tọa độ điểm 1 & điểm 2 tự động lưu vào config; thoát game hoặc chuyển server vào lại vùng đào vẫn nguyên vẹn!
- **Module Freecam tích hợp sẵn (Dedicated Freecam):** Skid chuẩn cơ chế Meteor, bay tự do 360° quan sát mỏ đào, có keybind phím `U` độc lập.
- **Tương thích tuyệt đối với Meteor Freecam:** Sửa dứt điểm lỗi xung đột góc nhìn camera khiến bot ngừng đào khi bật Freecam ngoài.
- **Thuật toán Pathfinding & Điều hướng thông minh:** Tự động di chuyển đến vị trí tối ưu, tránh chướng ngại vật, tự bắc cầu/nhảy khi cần.
- **Tự động sinh tồn (Survival Safety):** Tự ăn khi đói, ném bình kinh nghiệm (Exp Bottle) sửa cúp Mending, cảnh báo nham thạch/nước.
- **Hệ thống bảo vệ Staff Radar:** Quét người chơi và nhân viên quản trị (Staff) trong bán kính, tự đặt biển giải trình (Auto Sign), cảnh báo âm thanh và HUD.
- **Bộ lọc Block nâng cao (Visual Block Selector):** Hỗ trợ đầy đủ cả Whitelist (chỉ đào) và Blacklist (bỏ qua), chọn block trực quan bằng nút `[+]` và `[-]`.
- **Tích hợp Spotify & Discord Webhook:** Điều khiển nhạc Spotify ngay trong game và gửi báo cáo tiến độ đào trực tiếp về kênh Discord.

---

## 🎥 Module Freecam Riêng Biệt & Khắc Phục Lỗi Freecam Meteor

### 1. Tại sao trước đây bật Freecam (Meteor) thì Bot bị lỗi đào?
- **Nguyên nhân gốc rễ:** Trong Minecraft vanilla, phương thức đào `handleBlockBreaking()` phụ thuộc hoàn toàn vào biến `client.crosshairTarget` (tâm ngắm của Camera).
- Khi bạn bật Freecam của Meteor (hoặc bất kỳ mod camera nào), Camera bị tách rời khỏi người nhân vật và bay đi nơi khác. Lúc này, tâm ngắm `crosshairTarget` sẽ chỉ vào khoảng không hoặc một block cách xa 20–30 mét.
- Kết quả là Minecraft lập tức gọi `interactionManager.cancelBlockBreaking()`, hủy lệnh đào mỗi tick, đồng thời thuật toán bot bị kẹt ở trạng thái *"ngắm ô đào"* vì tưởng rằng người chơi chưa quay đầu nhìn vào block.

### 2. Cách giải quyết triệt để trong bản cập nhật này:
1. **Cô lập tâm ngắm đào bằng Mixin Redirect:** Trong `MinecraftClientMixin`, lệnh `handleBlockBreaking` đã được chuyển hướng (`@Redirect`) để luôn nhận diện chính xác `BlockHitResult` của khối block mục tiêu từ vị trí mắt của nhân vật thật (`player.getEyePos()`), bất kể Camera của bạn đang bay ở phương trời nào!
2. **Thuật toán ngắm động (`isAimingAt`):** Kiểm tra góc xoay và tia raycast xuất phát trực tiếp từ mắt nhân vật, không còn phụ thuộc vào vị trí camera tự do.
3. **Kết quả:** **BẠN CÓ THỂ DÙNG CẢ FREECAM CỦA METEOR LẪN FREECAM TÍCH HỢP CỦA AUTODAO MÀ KHÔNG HỀ BỊ LỖI ĐÀO NỮA!**

### 3. Hướng dẫn sử dụng Freecam tích hợp (AutoDao Freecam):
- **Tuỳ biến phím tắt thoải mái (Custom Keybind):**
  - **Đổi trực tiếp trong Menu `V`:** Bấm phím **`V`** -> mục **Tuỳ chọn** -> bấm vào nút **`Đổi phím Freecam: [...]`**, sau đó chỉ cần **bấm bất kỳ phím nào trên bàn phím bạn muốn** để gán (hoặc bấm `ESC / Delete / Backspace` để huỷ/xoá phím tắt).
  - **Đổi trong Minecraft Controls:** Vào *Options -> Controls -> Key Binds -> mục Miscellaneous -> `key.automine.freecam`*.
  - **Dùng lệnh chat:** Gõ chat `/freecam` hoặc `/automine freecam` để bật/tắt nhanh.
- **Nút bật/tắt trong Menu:** Bấm nút **`Freecam (Phím ...): [BẬT / TẮT]`**.
- **Chỉnh tốc độ bay:** Trong bảng **Tuỳ chọn**, có thanh **`Tốc độ bay Freecam`** (+ / - từ 1x đến 10x) để bạn chỉnh tốc độ lướt camera theo ý muốn.
- **Cách điều khiển camera khi Freecam BẬT (Cơ chế GLFW Direct Input - không bao giờ kẹt):**
  - **`W / A / S / D`:** Bay tới, lui, sang trái, sang phải theo đúng hướng camera đang ngắm (đã hỗ trợ chuẩn góc nhìn 3D).
  - **`Phím Cách (Space)`:** Bay thẳng lên trên.
  - **`Shift Trái / Phải (Sneak)`:** Hạ độ cao bay xuống dưới.
  - **`Ctrl Trái / Phải (Sprint)`:** Tăng tốc độ bay (Boost x2.5 tốc độ).
  - **Chuột:** Xoay nhìn 360° cực mượt, phản hồi tức thì 1:1 theo chuột.
- **Điểm ưu việt:** Khi bạn đang bay camera đi thám hiểm hang động hoặc soi quặng xung quanh, bot của bạn vẫn cặm cụi tự đi lại, đào quặng và nhặt đồ bình thường ở hậu trường!

---

## 📦 Hướng Dẫn Cài Đặt Từng Bước (Ai Cũng Làm Được)

Dưới đây là hướng dẫn chi tiết dành cho người chơi thông thường, đảm bảo ai cũng có thể cài đặt thành công chỉ sau 2 phút:

### Bước 1: Chuẩn bị Minecraft Fabric 1.21.11
1. Đảm bảo bạn đã cài đặt phiên bản **Minecraft 1.21.11** có cài **Fabric Loader** (phiên bản `0.16.x` trở lên).
   - Bạn có thể dùng bất kỳ Launcher nào: **Modrinth App**, **Prism Launcher**, **CurseForge**, **TLauncher** hoặc **Minecraft Launcher chính thức**.
2. Tải và cài đặt mod thư viện bắt buộc: **[Fabric API cho 1.21.11](https://modrinth.com/mod/fabric-api)**.

### Bước 2: Lấy file Mod AutoDao
- File mod đã được biên dịch sẵn nằm ở đường dẫn:  
  `build/libs/autodao-1.0.0.jar` (hoặc tải từ bản phát hành mới nhất).

### Bước 3: Bỏ file mod vào thư mục `mods`
- **Trên Windows:**
  1. Nhấn tổ hợp phím **`Windows + R`** trên bàn phím để mở hộp thoại *Run*.
  2. Nhập chính xác dòng chữ sau rồi bấm **Enter**:
     ```text
     %appdata%\.minecraft\mods
     ```
  3. *(Nếu bạn dùng Modrinth App hoặc Prism Launcher: Bấm chuột phải vào Profile game của bạn -> chọn **Open Folder** -> mở thư mục `mods`)*.
  4. Kéo thả file **`autodao-1.0.0.jar`** và file **`fabric-api-*.jar`** vào thư mục `mods` này.

### Bước 4: Khởi động Game & Thưởng thức
- Mở game lên, tham gia vào thế giới đơn hoặc máy chủ (Server).
- Nhìn vào góc màn hình nếu thấy các phím tắt hoặc gõ `/automine` mà menu hiện lên là đã cài đặt thành công 100%!

---

## 📖 Hướng Dẫn Sử Dụng Cầm Tay Chỉ Việc (Từ A đến Z)

> **Kịch bản mẫu:** Bạn muốn đào phẳng lì một cái hố to `10x10 block` và sâu `5 block`, chỉ nhặt quặng quý hoặc đào sạch đá để làm hầm căn cứ. Hãy làm theo 5 bước cực dễ sau:

### Bước 1: Chọn vùng cần đào (Đánh dấu 2 góc C và X)
1. Chạy nhân vật tới **góc thứ nhất** của khu đất bạn muốn đào.
2. Nhấn phím **`C`** trên bàn phím (hoặc gõ chat `/sel 1`).
   - *Game sẽ thông báo: Đã đặt điểm 1 tại tọa độ chân bạn.*
3. Chạy sang **góc đối diện chéo** (có thể trèo lên cao hoặc đào sâu xuống 5 block nếu muốn đào sâu).
4. Nhấn phím **`X`** trên bàn phím (hoặc gõ chat `/sel 2`).
   - *Game sẽ thông báo: Đã đặt điểm 2.*
5. Ngay lập tức, bạn sẽ thấy một **khung hộp 3D màu cam phát sáng** bao trùm toàn bộ khu vực bạn vừa chọn!

> 💾 **Tính năng Tự Động Lưu Vùng Chọn (Auto-Save Box Area):**  
> Ngay khi bạn bấm phím `C` hoặc `X` (hoặc dùng gậy wand, gõ `/sel 1` / `/sel 2`), tọa độ 2 điểm góc sẽ **ngay lập tức được tự động lưu vào file cấu hình `automine.properties`**.  
> - Dù bạn ngắt kết nối server, thoát game, đổi map hay khởi động lại máy tính, lần sau vào lại game **vùng chọn 3D màu cam sẽ tự động phục hồi nguyên vẹn** mà không cần phải chạy đi đánh dấu lại!  
> - Nếu bạn muốn bỏ vùng chọn hiện tại để chọn vùng mới, chỉ cần gõ lệnh chat `/sel clear` hoặc bấm phím tắt Xóa vùng chọn.


### Bước 2: Mở Menu Cài Đặt (Phím V)
1. Đứng yên và nhấn phím **`V`** trên bàn phím.
2. Một bảng điều khiển trực quan hiện ra:
   - **Cao mỗi tầng:** Mặc định là `3` (chuẩn nhất, đào từng tầng cao 3 block vừa tầm đầu).
   - **Rộng mặt đào:** Mặc định là `3`.
   - **Tầm với (block):** Để `4.0` hoặc `4.5`.
   - **Tự động ăn táo/thức ăn:** BẬT (để không bao giờ chết đói khi treo máy).
   - **Quăng exp sửa cúp:** BẬT (mang theo bình Exp trong túi đồ, khi cúp gần gãy bot sẽ tự ném exp xuống chân sửa đầy máu rồi đào tiếp).
   - **Freecam (Phím U):** Bật/tắt camera tự do.

### Bước 3: Cài đặt Bộ Lọc Block (Chỉ đào quặng hoặc Bỏ qua vật phẩm quý)
Nếu bạn muốn đào tất cả mọi thứ trong vùng, hãy bỏ qua bước này. Nếu bạn có yêu cầu đặc biệt:
1. Trong Menu phím `V`, nhìn sang khung **Lọc block** (màu vàng bên phải).
2. Bấm vào nút: **`✦ CHỌN & QUẢN LÝ BLOCK (+ / -) ➔`**.
3. Một kho block Minecraft 3D tuyệt đẹp sẽ hiện lên:
   - **Muốn CHỈ ĐÀO QUẶNG (Kim cương, Vàng, Sắt...):**
     - Bấm chọn Tab **`[★ Whitelist (Chỉ đào)]`**.
     - Ở bảng bên phải, bấm vào nút phân loại **`[Quặng]`** (hoặc gõ tên vào ô tìm kiếm).
     - Bấm dấu **`[+]`** màu xanh cạnh block bạn muốn đào. Block đó sẽ bay sang danh sách bên trái!
   - **Muốn ĐÀO HẾT trừ RƯƠNG hoặc OBSIDIAN:**
     - Bấm chọn Tab **`[⛔ Blacklist (Bỏ qua)]`**.
     - Tìm kiếm `chest` hoặc `obsidian` ở bảng bên phải và bấm dấu **`[+]`**.
   - **Nếu không muốn block đó nằm trong danh sách nữa:**
     - Chỉ cần bấm dấu **`[-]`** màu đỏ ở bảng danh sách bên trái để xóa ra ngay lập tức!
4. Bấm **Đóng** ở góc trên để lưu lại.

### Bước 4: Bắt đầu Đào & Treo Máy AFK
1. Nhấn phím **`H`** trên bàn phím (hoặc gõ chat `/start`).
2. Nhân vật sẽ tự động quay đầu, tìm đường đến khối đầu tiên và vung cúp đào thoăn thoắt!
3. **Đặc biệt:** Bạn có thể thoải mái bấm phím `V` để chỉnh sửa menu, mở túi đồ, nhấn phím **`U`** bay Freecam lượn vòng quanh xem bot làm việc, hoặc thậm chí **Alt-Tab ra ngoài lướt web / xem phim**, bot vẫn tiếp tục tự động đào ngầm bình thường mà không bị gián đoạn!

### Bước 5: Dừng đào
- Khi muốn dừng lại, chỉ cần nhấn phím **`H`** một lần nữa (hoặc gõ chat `/stop`).
- Nếu muốn xóa khung chọn màu cam, mở menu `V` và bấm **Xoá vùng**.

---

## 🎮 Phím Tắt & Toàn Bộ Lệnh Chat

### 1. Phím tắt thao tác nhanh (Keybinds)
*(Có thể đổi phím bất kỳ trong **Options -> Controls -> Key Binds**)*

| Phím | Chức năng | Ghi chú |
| :---: | :--- | :--- |
| **`V`** | Mở **Menu Cài Đặt Trung Tâm (AutoMine Main GUI)** | Bảng điều khiển chính |
| **`U`** | Bật / Tắt **Freecam (Camera Bay Tự Do)** | **Tính năng mới!** Skid từ Meteor, bay 360° |
| **`C`** | Đặt **Điểm 1 (Góc 1)** tại vị trí chân đang đứng | Đánh dấu tọa độ góc 1 |
| **`X`** | Đặt **Điểm 2 (Góc 2)** tại vị trí chân đang đứng | Đánh dấu tọa độ góc 2 |
| **`H`** | Bật / Tắt trạng thái đào (**Bắt đầu / Dừng lại**) | Khởi động hoặc tạm dừng bot |
| **`B`** | Mở giao diện điều khiển nhạc **Spotify Player HUD** | Next/Prev bài hát, chỉnh âm lượng |
| **`L`** | Mở màn hình thông tin phiên bản & Client | Xem trạng thái bản quyền offline |

---

### 2. Toàn bộ Lệnh Chat (`/` Commands)

#### ⛏️ Lệnh Điều Khiển Đào:
- `/start` : Bắt đầu đào vùng đã chọn.
- `/stop` : Dừng đào ngay lập tức.
- `/sel 1` : Đặt góc 1 tại tọa độ hiện tại.
- `/sel 2` : Đặt góc 2 tại tọa độ hiện tại.
- `/automine` : Mở nhanh menu cài đặt chính (giống phím `V`).

#### 🎯 Lệnh Bộ Lọc Block (Filter):
- `/filter toggle` : Bật hoặc tắt chức năng lọc block.
- `/filter mode` : Đổi qua lại giữa chế độ **Whitelist** và **Blacklist**.
- `/filter hand` : Thêm nhanh block đang cầm trên tay vào bộ lọc.
- `/filter add <id>` : Thêm block theo ID (ví dụ: `/filter add diamond_ore`).
- `/filter remove <id>` : Xóa block khỏi bộ lọc (ví dụ: `/filter remove diamond_ore`).
- `/filter list` : Xem toàn bộ các block đang có trong bộ lọc hiện tại.
- `/filter clear` : Xóa trắng danh sách lọc đang chọn.

#### 🛡️ Lệnh Quản Lý Bạn Bè (Friend Whitelist):
- `/friend add <tên>` : Thêm bạn bè (Radar Staff sẽ không báo động nhầm bạn bè).
- `/friend remove <tên>` : Xóa người chơi khỏi danh sách bạn bè.
- `/friend list` : Xem danh sách bạn bè đã thêm.

#### 📡 Lệnh Discord Webhook:
- `/webhook toggle` : Bật / tắt tính năng gửi báo cáo về Discord.
- `/webhook url <link>` : Cài đặt địa chỉ Webhook của bạn.
- `/webhook test` : Gửi một tin nhắn kiểm tra tới kênh Discord ngay lập tức.

---

## 📡 Hướng Dẫn Cài Đặt Báo Cáo Discord Webhook

Bạn muốn đi ngủ hoặc ra ngoài mà vẫn biết tài khoản ở nhà đã đào được bao nhiêu nghìn block, tọa độ đang ở đâu và cúp còn bao nhiêu máu? Hãy dùng tính năng Discord Webhook:

1. **Lấy link Webhook từ Discord:**
   - Mở ứng dụng Discord trên máy tính -> Vào server Discord của bạn.
   - Bấm vào biểu tượng **Cài đặt kênh (bánh răng)** ở kênh chat bạn muốn nhận thông báo.
   - Chọn mục **Tích hợp (Integrations)** -> Bấm **Tạo Webhook (Webhooks)**.
   - Bấm nút **Sao chép URL Webhook (Copy Webhook URL)**.
2. **Dán vào game:**
   - Cách 1: Gõ lệnh chat: `/webhook url <dán_link_webhook_vừa_copy_vào>`
   - Cách 2: Bấm phím **`V`** -> nhìn xuống ô **Báo cáo Webhook** -> dán link vào ô.
3. **Kiểm tra:**
   - Bấm nút **Gửi tin nhắn TEST** trong Menu hoặc gõ `/webhook test`.
   - Nếu thấy Discord thông báo có tin nhắn từ AutoMine gửi về là thành công! Cứ mỗi 5 phút hoặc từng mốc tiến độ, bot sẽ tự động gửi thống kê về điện thoại của bạn.

---

## 🏆 Nhật ký Hoàn thiện & Các Đột phá Kỹ thuật (Engineering Breakthroughs)

Quá trình giải mã và nâng cấp client trải qua một chuỗi các công đoạn kỹ thuật chuyên sâu:

### 1. Phá bỏ Xiềng xích DRM & Tái lập Độc lập 100% Offline
- **Thách thức:** Bản gốc bị khóa chặt bởi máy chủ xác thực từ xa qua cổng `2003` và bẫy sập `putAddress(0, 0)`. Khi mất mạng hoặc server offline, toàn bộ client bị crash.
- **Giải pháp:** Tái cấu trúc kiến trúc `IAutoMineCore` theo mô hình clean-room độc lập hoàn toàn. Toàn bộ logic giải thuật đào, phân lớp vùng chọn và quản lý luồng được viết lại bằng Java 21 thuần túy, loại bỏ mọi kết nối gián điệp và bẫy crash ngầm.

### 2. Module Freecam Tích hợp & Sửa lỗi Xung đột Meteor Freecam
- **Thách thức:** Khi người chơi bật Freecam của Meteor, Camera bị tách khỏi nhân vật khiến `client.crosshairTarget` trỏ vào hư vô. Minecraft vanilla liên tục hủy lệnh đào (`cancelBlockBreaking`), khiến bot bị đứng im hoặc đào không vỡ block.
- **Giải pháp:**
  - Tích hợp module **AutoMineFreecam** độc quyền với đầy đủ phím tắt (`U`), điều hướng 3D mượt mà, bay xuyên tường, tách biệt hoàn toàn input của camera và nhân vật.
  - Sử dụng Mixin can thiệp `@Redirect` vào lệnh đọc `crosshairTarget` trong `handleBlockBreaking`. Lệnh đào sẽ luôn nhận diện đúng tọa độ khối đang đào từ góc nhìn mắt nhân vật thật.
  - **Tương thích cả 2:** Người chơi dùng Freecam của AutoDao hay Freecam của Meteor thì bot vẫn đào cực mượt mà không bao giờ bị ngắt quãng!

### 3. Công nghệ Đào ngầm Xuyên GUI & Unfocused (Background Mining)
- **Thách thức:** Ở bản gốc, chỉ cần người chơi bấm phím mở Menu cài đặt hoặc Alt-Tab ra màn hình desktop là bot lập tức ngừng đào.
- **Giải pháp:** Can thiệp sâu vào vòng lặp tick của Fabric qua Mixin, vô hiệu hóa cờ `pauseOnLostFocus` và chặn sự kiện tạm dừng của `openGameMenu`. Bot duy trì nhịp độ đào liên tục 24/7 kể cả khi mở menu hay chuyển tab.

### 4. Triệt tiêu Lỗi Gói tin Kép 2x Speedmine (Ghost Block Desync)
- **Thách thức:** Khi đóng menu GUI, `handleInputEvents()` gọi đào và hook cuối tick cũng gọi tiếp lần thứ hai, khiến gói tin gửi lên nhanh gấp đôi (2x speedmine), server từ chối phá block và không rơi tài nguyên.
- **Giải pháp:** Thiết kế cờ tick độc quyền `@Unique private boolean automine$breakingHandledThisTick`. Nếu trong tick đó game đã gọi phá block rồi thì hook cuối tick sẽ bỏ qua, đảm bảo đúng 1 lần đào/tick (1x tốc độ chuẩn), tương thích hoàn hảo với mọi anti-cheat server.

### 5. Bộ lọc Block Trực quan Độc lập: Whitelist & Blacklist (Visual Block Selector)
- **Thách thức:** Nhập ID bằng văn bản ở bản cũ rất dễ gõ sai tên ID block.
- **Giải pháp:** Tách bạch 2 danh sách **Whitelist** và **Blacklist**, tích hợp giao diện duyệt icon 3D toàn bộ block trong Minecraft, thêm block bằng nút xanh **`[+]`** và xóa bằng nút đỏ **`[-]`**.

### 6. Đập tan Deadlock Điều hướng & Thuật toán Chống Kẹt (Anti-Stuck Watchdog)
- **Thách thức:** Khi đứng trên nóc block cần đào, bot bị rơi vào vòng lặp vô tận (đứng nhìn block mà không đào được).
- **Giải pháp:** Tích hợp tính toán tầm nhìn động (Dynamic Line-of-Sight Offsets) và cơ chế **Stuck Watchdog (3 giây)**: Nếu phát hiện nhân vật bị kẹt quá 3 giây tại một tọa độ, bot sẽ tự động khoan thẳng vào các block cản đường phía trước để tự giải phóng bản thân.

---

## ⚡ So sánh Trước và Sau (Before & After)

| Tính năng | Bản gốc (Before Deobf) | Bản hiện tại (After Deobf & Enhanced) |
| :--- | :--- | :--- |
| **Bản quyền / DRM** | Bắt buộc kết nối máy chủ xác thực, nguy cơ văng game | **100% Offline**, chạy độc lập, mã nguồn sạch hoàn toàn |
| **Freecam** | Không có Freecam riêng, xung đột nặng với Meteor Freecam | **Tích hợp Freecam riêng (phím U)** + **Sửa triệt để lỗi xung đột với Meteor** |
| **Lọc Block (Filter)** | Chỉ có Blacklist cơ bản, gõ ID bằng chữ dễ sai | **Hỗ trợ cả Whitelist & Blacklist**, giao diện 3D trực quan, nút `[+]` và `[-]` |
| **Treo máy nền (AFK)** | Dừng đào ngay khi bật Menu GUI hoặc Alt-Tab ra ngoài | **Đào ngầm liên tục (Background Mining)** bất chấp mở GUI hay chuyển tab |
| **Lưu vùng đào (Box Area)** | Thoát game hoặc ngắt kết nối là mất sạch vùng chọn | **Tự động lưu (Auto-Save Box Area)**: Điểm 1 & 2 được lưu vĩnh viễn vào config |
| **Tốc độ phá block** | Dễ bị lỗi gói tin kép 2x speedmine khiến server chặn rơi đồ | **Đồng bộ nhịp tick hoàn hảo (1x chuẩn)**, đào vỡ và nhặt tài nguyên 100% |
| **Báo cáo tiến độ** | Không có | **Tích hợp Discord Webhook**: Báo cáo số block, tốc độ, tọa độ về điện thoại |
| **Độ ổn định khi đào** | Dễ bị đứng im (stuck) khi đứng trùng tọa độ mục tiêu | **Thuật toán chống kẹt (Anti-Stuck Watchdog)**, tự đổi góc đào và tìm đường bù |
| **Bảo vệ Bạn bè** | Quét staff thô sơ, nhận diện nhầm bạn bè trong tầm | **Danh sách Bạn bè (Friend Whitelist)**: Không kích hoạt auto sign khi có bạn bè |
| **Spotify HUD** | Dễ văng game (JNA error) khi không tìm thấy Spotify | **Tự bọc ngoại lệ an toàn**, điều khiển mượt mà, lưu vị trí kéo thả |

---

## 🛠 Tính Năng Chi Tiết (Features Breakdown)

### 1. Đào tự động thông minh (Autonomous Mining Engine)
- Chọn vùng đào bằng 2 điểm góc (`Point 1` & `Point 2`) tạo thành khối hộp 3D.
- **Tự động lưu vùng chọn (Auto-Save Box Area):** Ngay khi bạn bấm `C`, `X` hoặc chọn trong menu, tọa độ hộp đào 3D được tự động ghi nhận vào `automine.properties`. Khi vào lại game, khung cam tự động hiển thị lại mà không cần đánh dấu lại từ đầu!
- Tự động chia lớp (`layerHeight`, `passWidth`) và đào quét sạch (`sweepLayer`).
- Khi đào, nhân vật tự xoay hướng nhìn mượt mà (smooth yaw/pitch) giống người chơi thật, chống anti-cheat giật góc nhìn.
- Tự động nhảy, bắc block đệm (`allowPlace`) nếu hụt chân hoặc cần trèo lên vị trí cao hơn.

### 2. Freecam Bay Tự Do Độc Quyền (Built-in Freecam Module)
- Phím tắt mặc định: **`U`** (có thể cấu hình lại trong phần Controls).
- Tách biệt chuyển động của camera và thân thể nhân vật: Camera tự do bay lượn ngắm cảnh, bot vẫn tiếp tục đi lại đào quặng dưới lòng đất.
- Hỗ trợ đầy đủ phím tăng tốc (Ctrl) và nâng/hạ độ cao (Space / Shift).
- Hiển thị đầy đủ mô hình 3D nhân vật đang đào, trang bị và hoạt ảnh vung tay mượt mà.

### 3. Bộ lọc Block trực quan (Visual Block Selector)
- **Whitelist Mode (Chỉ đào):** Chỉ đào những block được chọn (ví dụ: chỉ đào Quặng Kim Cương, Quặng Vàng, Đá Sâu...).
- **Blacklist Mode (Bỏ qua):** Đào toàn bộ vùng trừ những block nằm trong danh sách (ví dụ: né Obsidian, Spawner, Rương, Khối Cửa...).
- Thêm block nhanh bằng nút `[+]`, gỡ block bằng nút `[-]`, hỗ trợ lọc theo Quặng, Đá, Gỗ và tìm kiếm tên block thời gian thực.

### 4. An toàn sinh tồn (Survival Automation)
- **Auto Eat:** Tự phát hiện thức ăn trong thanh hotbar/túi đồ và ăn khi độ đói xuống dưới ngưỡng cài đặt.
- **Exp Repair (Mending):** Khi cúp sắp hỏng, bot tự động dừng đào, chuyển sang bình Exp trong người, ném xuống chân sửa cúp đến khi đầy máu rồi tiếp tục đào.
- **Hazard Alerts:** Tự động phát hiện dung nham hoặc dòng nước chảy vào vị trí đào và cảnh báo người chơi.

### 5. Staff Radar & Chống Soi
- Quét danh sách Staff trong máy chủ và hiển thị HUD trạng thái chấm xanh (online) / xám (offline).
- Khi Staff tiếp cận trong bán kính nguy hiểm: Tự động đặt biển giải trình (`Auto Sign`) với nội dung cài đặt sẵn (vd: *"Mình AFK đào đá, không hack..."*).

---

## 💻 Yêu cầu hệ thống & Hướng dẫn Tự Biên Dịch (Build from Source)

Nếu bạn là lập trình viên hoặc muốn tự tay biên dịch bản build từ mã nguồn:

### Yêu cầu môi trường:
- **Hệ điều hành:** Windows 10 / 11, Linux, macOS.
- **JDK:** **Java Development Kit 21** (Khuyên dùng [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21) hoặc Oracle JDK 21).
- **Gradle:** Đã có sẵn Gradle Wrapper 9.x trong repo (không cần cài trước).

### Các bước biên dịch:
1. Mở PowerShell / Terminal tại thư mục gốc của project:
   ```bash
   # Kiểm tra Java phiên bản 21
   java -version
   ```
2. Thực hiện lệnh biên dịch:
   ```bash
   # Biên dịch và đóng gói file JAR
   .\gradlew remapJar --no-daemon
   ```
3. Sau khi build hoàn tất (`BUILD SUCCESSFUL`), file mod sẽ xuất hiện tại:
   ```text
   build/libs/autodao-1.0.0.jar
   ```

---

## ❓ Câu Hỏi Thường Gặp & Xử Lý Sự Cố (FAQ)

#### Q1: Tôi có thể vừa bật Freecam vừa để bot tự đào được không?
> **Trả lời:** **HOÀN TOÀN ĐƯỢC!** Bạn có thể nhấn phím **`U`** để mở Freecam riêng của AutoDao, hoặc bật Freecam của Meteor. Bot sẽ vẫn tự động di chuyển và đào khối chính xác 100% ở phía dưới mà không hề bị ngắt quãng hay lỗi rơi đồ.

#### Q2: Tại sao tôi bấm phím H hoặc gõ `/start` mà nhân vật không chịu đào?
> **Trả lời:** Bạn chưa đánh dấu đủ 2 điểm góc! Hãy chạy tới góc thứ nhất bấm **`C`**, chạy sang góc đối diện bấm **`X`** để hiện khung màu cam lên trước, sau đó mới bấm **`H`**.

#### Q3: Tại sao khi đào block bị nứt rất nhanh nhưng không vỡ, hoặc vỡ ra lại biến mất và không rơi đồ?
> **Trả lời:** Đây là hiện tượng lỗi gửi gói tin kép 2x Speedmine ở bản cũ. Hãy chắc chắn rằng bạn đang sử dụng bản build mới nhất của AutoDao (đã được vá triệt để cơ chế tick đồng bộ 1x).

#### Q4: Bot bị đứng im nhìn một chỗ thì xử lý thế nào?
> **Trả lời:** Bot đã có cơ chế Anti-Stuck tự gỡ sau 3 giây. Nếu vị trí đứng quá hẹp, hãy bấm phím `V` và bật tùy chọn **"Xây trụ leo lên"** để bot tự bắc block nhảy lên vị trí thoáng hơn.

#### Q5: Làm thế nào để bot không đào vỡ rương đồ của tôi?
> **Trả lời:** Bấm phím **`V`** -> chọn **Bộ lọc block** -> chuyển sang tab **Blacklist** -> tìm kiếm `chest` -> bấm dấu **`[+]`**. Giờ đây bot sẽ né toàn bộ rương ra!

---

## ⚠️ Lưu Ý Quan Trọng & Lời Nhắn Gửi Chân Thành

### 📌 Lưu ý cốt lõi khi chọn vùng đào:
- **Hãy luôn chọn vùng trong một khối hộp chữ nhật / khối vuông thẳng thớm:** Khi đánh dấu 2 điểm **`C`** và **`X`**, hãy đảm bảo không gian đào là một khối hộp liền mạch và vuông vắn.
- **Tránh để lại các mảnh ghép zic-zac hoặc lồi lõm quá phức tạp:** Việc để lại địa hình zic-zac gồ ghề hoặc các ngóc ngách hiểm trở sẽ khiến thuật toán tìm đường (Pathfinding) bị bối rối, dẫn đến việc bot tính toán đường đi sai hoặc đào lệch hướng -> **khiến bot bị "chửi oan" tội nghiệp!** 🥺
- Hãy dọn sơ các chướng ngại vật quá dị thường hoặc cho bot một không gian hình hộp vuông vắn để bot có thể phát huy 100% công suất đào mượt mà nhất.

### 💌 Lời Xin Lỗi & Tri Ân từ Đáy Lòng:
> *"Nếu trong quá trình sử dụng, bot có lỡ đào sai, đào lệch một vài block hoặc chưa hoàn toàn đúng 100% theo ý muốn của bạn, thì cho bot gửi một lời xin lỗi chân thành nhất! 🙇‍♂️*  
> *Mong rằng bạn sẽ rộng lòng tha thứ cho sự vụng về nhỏ này của bot. Khi gặp tình huống đó, bạn chỉ cần bấm **`H`** để dừng lại, xóa vùng chọn cũ, chọn lại góc vuông vắn theo đúng ý mình và bấm **`H`** để bot làm lại từ đầu nhé.*  
> *Chân thành cảm ơn sự đồng hành và thấu hiểu của bạn. Chúc bạn có những giờ phút cày cuốc tài nguyên thật bội thu và vui vẻ! Arigatou gozaimasu! (ありがとうございます ❤️)"*

---

## 📂 Cấu trúc Thư mục & Mã Nguồn (Project Structure)

```text
AutoDao-SRC-DEOBF/
├── src/main/java/com/automine/
│   ├── AutoMineClient.java            # Entrypoint Fabric client mod & đăng ký phím tắt (V, U, C, X, H...)
│   ├── Bridge.java                    # Cầu nối dữ liệu luồng khai thác & Mixin
│   ├── core/
│   │   ├── AutoMineFreecam.java       # Module Freecam bay tự do, tính toán vận tốc & góc nhìn
│   │   ├── AutoMineConfig.java        # Quản lý cấu hình, lưu trữ Whitelist/Blacklist, Webhook
│   │   ├── AutoMineEngine.java        # Động cơ đào, điều hướng, vòng lặp tick, kiểm tra mục tiêu
│   │   ├── AutoMineMiningStrategy.java# Chiến thuật phá block, góc nhìn mượt, gỡ kẹt
│   │   ├── AutoMineBlockHelper.java   # Phân tích hình học block & ngắm bắn độc lập camera (isAimingAt)
│   │   ├── AutoMineCommands.java      # Hệ thống lệnh chat ClientCommand
│   │   ├── AutoMineStaffDetector.java # Radar quét Staff, tự đặt biển Auto Sign
│   │   ├── AutoMineProgressNotifier.java # Báo cáo tiến độ đào qua Discord Webhook
│   │   └── SpotifyApiBridge.java      # Tích hợp điều khiển Spotify trên Windows
│   ├── gui/
│   │   ├── AutoMineMainScreen.java    # Giao diện menu cài đặt trung tâm (Phím V) & nút toggle Freecam
│   │   ├── AutoMineBlockFilterScreen.java # Giao diện chọn block Whitelist/Blacklist trực quan
│   │   └── AutoMineSpotifyScreen.java # Giao diện Spotify Player & chọn bài hát
│   └── mixin/
│       ├── CameraMixin.java           # Can thiệp vị trí Camera cho Freecam & hiển thị thân nhân vật
│       ├── MouseMixin.java            # Điều hướng góc quay chuột vào camera tự do khi Freecam bật
│       ├── KeyboardInputMixin.java    # Khóa input di chuyển nhân vật khi người chơi đang lái camera
│       └── MinecraftClientMixin.java  # Đào ngầm xuyên GUI, đồng bộ tick 1x & fix lỗi Freecam
├── build.gradle                       # Cấu hình Fabric Loom và các phụ thuộc
├── gradle.properties                  # Định nghĩa phiên bản Minecraft 1.21.11 & Yarn
└── README.md                          # Tài liệu hướng dẫn đầy đủ
```

---

<p align="center">
  <i>Được hoàn thiện và tối ưu hóa bởi AI Agentic Reverse Engineering System.</i>
</p>
