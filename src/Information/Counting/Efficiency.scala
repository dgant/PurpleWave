package Information.Counting

import Lifecycle.With

class Efficiency {
  var _framesSupplyBlocked              : Int = 0
  var _framesSupplyBlockedConsecutively : Int = 0
  var _framesOversupplied               : Int = 0
  var _framesOversuppliedConsecutively  : Int = 0
  def framesSupplyBlocked               : Int = _framesSupplyBlocked
  def framesSupplyBlockedConsecutively  : Int = _framesSupplyBlockedConsecutively
  def framesOversupplied                : Int = _framesOversupplied
  def framesOversuppliedConsecutively   : Int = _framesOversuppliedConsecutively

  def update(): Unit = {
    if ( ! With.configuration.debugging) return
    val supplyAvailable = With.self.supplyTotal400 - With.self.supplyUsed400
    val producers       = With.units.ours.filter(u => u.complete && u.framesIdleConsecutive > 0 && u.unitClass.unitsTrained.exists(_.supplyRequired > 0))
    val supplyBlocked   = producers.find(p => p.unitClass.unitsTrained.iterator
      .filter(_.supplyRequired > 0)
      .exists(t =>
            t.supplyRequired  > supplyAvailable
          && t.mineralPrice   < With.self.minerals
          && t.gasPrice       < With.self.gas))
    if (supplyBlocked.isDefined && With.self.supplyTotal400 < 400) {
      _framesSupplyBlocked += 1
      _framesSupplyBlockedConsecutively += 1
    } else {
      _framesSupplyBlockedConsecutively = 0
    }

    if (With.self.supplyTotal400 >= With.self.supplyUsed400 + 8 * 2 * 2) {
      _framesOversupplied += 1
      _framesOversuppliedConsecutively += 1
    } else {
      _framesOversuppliedConsecutively = 0
    }
  }
}
