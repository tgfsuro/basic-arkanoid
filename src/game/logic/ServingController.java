/** kê bóng: lắc trái phải trên pad, space để bắn bóng*/
package game.logic;

import game.objects.Ball;
import game.objects.Paddle;

/** Quản lý trạng thái “serving” (kê bóng) & bắn bóng. */
public class ServingController {

    private boolean serving = true;
    private double serveOffset = 0; // lệch so với tâm paddle
    private int serveDir = +1;      // +1 phải, -1 trái
    private double serveSpeed = 1.8;

    public boolean isServing() { return serving; }

    /** Đặt vào trạng thái kê bóng (giữ PLAY). */
    public void enterServing(Paddle paddle, Ball ball) {
        serving = true;
        serveOffset = 0;
        serveDir = +1;
        // neo bóng lên paddle tức thì
        ball.vx = 0; ball.vy = 0;
        ball.x  = paddle.x + paddle.w / 2.0;
        ball.y  = paddle.y - ball.r - 1;
    }

    /** Cập nhật trượt trái–phải khi đang serving. */
    public void updateWhileServing(Paddle paddle, Ball ball) {
        double maxOffset = paddle.w / 2.0 - ball.r;
        serveOffset += serveDir * serveSpeed;
        if (serveOffset > maxOffset) { serveOffset = maxOffset; serveDir = -1; }
        if (serveOffset < -maxOffset){ serveOffset = -maxOffset; serveDir = +1; }

        ball.x = paddle.x + paddle.w / 2.0 + serveOffset;
        ball.y = paddle.y - ball.r - 1;
    }

    /** SPACE để bắn bóng; trả về true nếu đã bắn. */
    public boolean launch(Paddle paddle, Ball ball) {
        if (!serving) return false;
        double center = paddle.x + paddle.w / 2.0;
        double t = (ball.x - center) / (paddle.w / 2.0); // -1..1
        t = Math.max(-1, Math.min(1, t));
        double speed = 6.0;
        ball.vx = t * 5.0;
        ball.vy = -Math.sqrt(Math.max(1, speed * speed - ball.vx * ball.vx));
        serving = false;
        return true;
    }
}
