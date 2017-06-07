package Planning.Plans.Information

import Planning.Plans.Compound.{And, IfThenElse, Not}
import Planning.Plans.Macro.Milestones.SupplyAtLeastDoubleThis

class ScoutAt(minimumSupply:Int)
  extends IfThenElse(
    new And(
      new SupplyAtLeastDoubleThis(minimumSupply),
      new Not(new FoundEnemyBase)
    ),
    new FindEnemyBase) {
  
  description.set("Find an enemy base at " + minimumSupply + " supply")
}
