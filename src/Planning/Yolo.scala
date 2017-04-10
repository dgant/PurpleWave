package Planning

import Lifecycle.With

object Yolo {
  def enabled   : Boolean = With.supplyUsed > 192 * 2 && With.minerals > 1000
  def disabled  : Boolean = ! enabled
}
