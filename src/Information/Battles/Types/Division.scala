package Information.Battles.Types

import Information.Geography.Types.Base
import ProxyBwapi.UnitInfo.UnitInfo
import Tactic.Squads.UnitGroup

/*
  A Division is a clustering of enemies, associated with the map features relevant to them.
  An enemy unit should only be in one Division.
  This may require merging divisions into super-divisions:
  - If Battle 1 encompasses zones A and B
  - and Battle 2 encompasses zones B and C
  - then these two battles need to be part of the same division
 */
case class Division(enemies: Iterable[UnitInfo], bases: Set[Base]) extends UnitGroup {
  def merge(other: Division): Division = Division(enemies ++ other.enemies, bases ++ other.bases)

  override def groupUnits: Seq[UnitInfo] = enemies.toSeq
}