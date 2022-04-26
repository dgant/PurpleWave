package Placement.Templating

import Lifecycle.With
import Mathematics.Points.{Direction, Tile}
import bwapi.Race

trait TemplateFilter {
  private var mineralDirection  : Iterable[Direction] = Iterable.empty
  private var gasDirection      : Iterable[Direction] = Iterable.empty
  private var exitDirection     : Iterable[Direction] = Iterable.empty
  private var races             : Iterable[Race]      = Iterable.empty
  private var enemyRaces        : Iterable[Race]      = Iterable.empty

  def withMineralDirection  (values: Direction*): Template = { mineralDirection ++= values; this.asInstanceOf[Template] }
  def withGasDirection      (values: Direction*): Template = { gasDirection     ++= values; this.asInstanceOf[Template] }
  def withExitDirection     (values: Direction*): Template = { exitDirection    ++= values; this.asInstanceOf[Template] }
  def withRaces             (values: Race*)     : Template = { races            ++= values; this.asInstanceOf[Template] }
  def withEnemyRaces        (values: Race*)     : Template = { enemyRaces       ++= values; this.asInstanceOf[Template] }

  def accept(tile: Tile): Boolean = {
    if (mineralDirection.nonEmpty && ! tile.base.map(_.mineralDirection)  .exists(d => mineralDirection .exists(d==))) return false
    if (gasDirection.nonEmpty     && ! tile.base.map(_.gasDirection)      .exists(d => gasDirection     .exists(d==))) return false
    if (exitDirection.nonEmpty    && ! tile.zone.exitDirection            .exists(d => exitDirection    .exists(d==))) return false
    if (races.nonEmpty            && ! races.exists(_ == With.self.raceCurrent)) return false
    if (enemyRaces.nonEmpty       && ! enemyRaces.exists(With.enemies.map(_.raceInitial).contains)) return false
    true
  }
}
