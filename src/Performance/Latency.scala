package Performance

import Lifecycle.With
import Utilities.?

final class Latency {

  @inline def latencyFrames   : Int = _latencyFrames
  @inline def remainingFrames : Int = _remainingFrames
  @inline def turnSize        : Int = _turnSize

  private var _latencyFrames      : Int = _
  private var _remainingFrames    : Int = _
  private var _turnSize           : Int = _
  private var _minRemainingFrames : Int = latencyFrames

  def onFrame(): Unit = {
    // This implementation is futureproofed for Remastered support for changing latency
    val previousLatencyFrames = _latencyFrames
    _latencyFrames = With.game.getLatencyFrames
    _remainingFrames = With.game.getRemainingLatencyFrames

    if (_latencyFrames != previousLatencyFrames) {
      _minRemainingFrames = latencyFrames
    }
    _minRemainingFrames = Math.min(_minRemainingFrames, With.game.getRemainingLatencyFrames)
    _turnSize =
      ?(latencyFrames <= 3,
        1,
        ?(latencyFrames == 6,
          2,
          1 + Math.max(0, With.game.getLatencyFrames - _minRemainingFrames)))
  }

  onFrame()
}
