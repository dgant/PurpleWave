package Information.GameSense

import Debugging.SimpleString

trait GameSense extends SimpleString {
  private var _opposite: Option[GameSense] = None
  def opposite: Option[GameSense] = _opposite
  def setOpposite(argOpposite: GameSense): Unit = {
    _opposite = Some(argOpposite)
  }
}
