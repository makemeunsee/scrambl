import com.badlogic.gdx.Gdx
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

package object threading {
  
  // wait and check every 50ms if waiting should be skipped
  def skippableWait(waitingTime: Long)(implicit cancelBox: CancelBox) = {
    val t0 = System.currentTimeMillis
    while (!cancelBox.cancelled && System.currentTimeMillis < t0 + waitingTime ) {
      Thread.sleep(50)
    }
  }

  // wait some delay then apply action on gdx thread
  def delayedGdxAction(delay: Long, action: => Unit) {
    Future {
      Thread.sleep(delay)
    } onComplete { _ =>
      onGdxThread(action)
    }
  }

  // wait some delay (skippable through a cancel box) then apply action on gdx thread
  def skippableDelayedGdxAction(delay: Long, action: => Unit)(implicit cancelBox: CancelBox) {
    Future {
      skippableWait(delay)
    } onComplete { _ =>
      onGdxThread(action)
    }
  }
  
  // execute an action on the gdx main thread (which provides GL context)
  def onGdxThread(action: => Unit) {
    Gdx.app.postRunnable(new Runnable {
      override def run() {
        action
      }
    })
  }
}