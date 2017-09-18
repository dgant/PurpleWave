package Planning.Plans.Macro.Expanding

import Information.Geography.Types.Base
import Lifecycle.With

class BuildCannonsAtExpansions(initialCount: Int) extends BuildCannonsAtBases(initialCount) {
  
  override def eligibleBases: Iterable[Base] = {
    val settling = With.units.ours
      .filter(u => u.agent.toBuild.exists(_.isTownHall))
      .flatMap(u => u.agent.toBuildTile.map(tile =>
        tile.zone.bases.find(base => base.townHallTile == tile)))
      .flatten
    
    With.geography.bases
      .filter(base => base.owner.isUs || settling.contains(base))
      .filterNot(_ == With.geography.ourMain)
      .filterNot(_ == With.geography.ourNatural)
  }
}
