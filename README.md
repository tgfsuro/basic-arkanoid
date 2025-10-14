# Game Project

## Mô tả
Dự án này là một trò chơi đơn giản sử dụng Java Swing, bao gồm các thành phần chính từ việc khởi động ứng dụng cho đến việc xử lý logic của trò chơi.

## Cấu trúc Dự án

### 1) `src/game/Main.java`
- **Nhiệm vụ:** Điểm khởi động ứng dụng.
- **Chính:** Gọi `SwingUtilities.invokeLater(...)` để tạo UI trên **EDT** (Event Dispatch Thread) — đây là best practice của Swing, tránh lỗi luồng.
- **Luồng:** Tạo một `GameWindow` → cửa sổ hiện lên ngay.

### 2) `src/game/GameWindow.java`
- **Nhiệm vụ:** Cửa sổ game (kế thừa `JFrame`).
- **Chính:**
    - Đặt tiêu đề.
    - `setDefaultCloseOperation(EXIT_ON_CLOSE)`.
    - `setResizable(false)`.
    - Tạo `GamePanel(width=800, height=600)` và `setContentPane(panel)`.
    - Gọi `pack()` để khớp kích thước với `PreferredSize` của panel.
    - `setLocationRelativeTo(null)` để đưa cửa sổ ra giữa màn hình.
    - `setVisible(true)` để hiển thị.

### 3) `src/game/GamePanel.java`
- **Nhiệm vụ:** Toàn bộ vòng đời game: nhận phím, cập nhật vật lý/va chạm, vẽ.
- **Thuộc tính chính:**
    - Kích thước màn hình `WIDTH`, `HEIGHT`.
    - Timer (khoảng 16ms) chạy ~60 FPS → gọi `actionPerformed`.
    - Đối tượng: `Paddle paddle`, `Ball ball`, mảng `Brick[] bricks`.
    - Cấu hình “basic”: `cols = 10`, `rows = 5`, `brickGap`, `brickTop`.
    - Trạng thái input: `left`, `right` (cờ nhấn phím).

- **Khởi tạo:**
    - Set nền đen, `setFocusable(true)` và `addKeyListener(this)` để nhận phím.
    - Tạo paddle ở đáy, bóng ngay trên paddle, tạo lưới gạch bằng `makeBricks()`.
    - Start Timer để tick đều.

#### Hàm `makeBricks()`
- Tính bề rộng khả dụng, từ đó tính `brickWidth`/`brickHeight` và tọa độ bắt đầu giúp lưới gạch căn giữa.
- Tạo mảng một chiều `rows * cols`.

#### Vòng lặp (actionPerformed → updateGame → repaint):
- **Di chuyển paddle:**
    - Nếu `left/right` true → gọi `paddle.move(dir, WIDTH)` và clamp trong màn hình.

- **Cập nhật bóng:**
    - `ball.update()` cộng vận tốc vào vị trí.

- **Va tường:**
    - Nếu chạm biên trái/phải → đảo `vx`; chạm trần → đảo `vy`. Có clamp vị trí để khỏi “lọt tường”.

- **Rơi đáy:**
    - Nếu tâm bóng vượt đáy → reset bóng về vị trí ban đầu (bản basic chưa có lives).

- **Va paddle (đơn giản):**
    - Kiểm tra intersects giữa `ball.getRect()` và `paddle.getRect()`.
    - Nếu đang đi xuống (`vy > 0`) thì:
        - Đặt bóng ngay trên paddle (tránh kẹt).
        - Đảo `vy`.
        - Cho chút “điều hướng”: đang giữ trái/phải thì chỉnh nhẹ `vx`.

- **Va gạch:**
    - Duyệt mảng, gặp gạch nào mà intersects thì:
        - Xóa gạch (gán phần tử mảng = `null`).
        - Đảo `vy`.
        - `break;` (mỗi tick chỉ xử lý 1 va chạm cho đơn giản).

- **Vẽ (paintComponent):**
    - Bật antialiasing cơ bản.
    - Vẽ dòng hướng dẫn.
    - Duyệt mảng gạch, gạch nào còn thì gọi `b.draw(g2)`.
    - Vẽ paddle và bóng.

#### Input (KeyListener):
- `keyPressed`: nếu phím trái/phải → đặt cờ `left/right = true`.
- `keyReleased`: thả phím → cờ về `false`.
- **Lý do dùng cờ:** giúp giữ phím mượt (thay vì bắt sự kiện tức thời).

**Giới hạn cố ý (để “basic”):** Va chạm đơn giản (đảo `vy` cho brick), không phân tích cạnh/tâm va chạm; 1 va chạm/frame; không có score/lives/state machine.

### 4) `src/game/objects/Paddle.java`
- **Nhiệm vụ:** Đại diện thanh đỡ.
- **Thuộc tính:** `x`, `y` (góc trái-trên), `w`, `h`, `speed`.
- **Hàm `move(dir, screenW)`**: cộng `x += dir * speed` và giới hạn trong khung (chừa mép 10px).
- **`getRect()`:** trả về `Rectangle` để dễ check va chạm.
- **`draw(...)`:** vẽ hình chữ nhật bo tròn màu xám sáng.

### 5) `src/game/objects/Ball.java`
- **Nhiệm vụ:** Quả bóng.
- **Thuộc tính:** Tâm `x`, `y`, bán kính `r`, vận tốc `vx`, `vy`.
- **`update()`:** cộng vận tốc vào vị trí mỗi frame (vật lý tuyến tính).
- **`getRect()`:** trả về bounding box (hình chữ nhật bao quanh hình tròn) — đủ dùng cho bản basic.
- **`draw(...)`:** vẽ hình tròn trắng.
- **Lưu ý:** Dùng bounding box thay vì va chạm tròn-chữ nhật chính xác để giản lược. Ở tốc độ hiện tại + 60FPS vẫn ổn.

### 6) `src/game/objects/Brick.java`
- **Nhiệm vụ:** Một viên gạch.
- **Thuộc tính:** `x`, `y`, `w`, `h` (hình chữ nhật).
- **`getRect()`:** hitbox để va chạm.
- **`draw(...)`:** vẽ hình chữ nhật màu xanh nhạt + viền mờ.

## Luồng tick “từ trên xuống” (tóm tắt)
- **Timer tick** → `actionPerformed`.
- **`updateGame()`:**
    - Đọc cờ phím → di chuyển paddle.
    - Cập nhật bóng → kiểm tra tường, đáy, va paddle, va gạch.
- **Vẽ khung hình (`repaint` → `paintComponent`):**
    - Text hướng dẫn → gạch → paddle → bóng.

Lặp lại ~60 lần/giây.
