package Information

import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech

import scala.collection.mutable

trait EnemyTechs {
  lazy val techsOwned: Map[PlayerInfo, mutable.HashSet[Tech]] = With.enemies.map(e => (e, new mutable.HashSet[Tech])).toMap

  protected def updateTechs(): Unit = {
    val ours = With.units.ours
    With.enemies.foreach(enemy => {
      def addIf(tech: Tech, boolean: Boolean): Unit = {
        if (boolean && tech.getRace == enemy.raceCurrent) {
          techsOwned(enemy) += tech
        }
      }

      val units = With.units.enemy.filter(_.player == enemy)

      // The checks based on unit statuses aren't brilliant;
      // we could proc them on ourselves
      // or fail to proc on an enemy who missed while casting
      addIf(Terran.GhostCloak, units.exists(u => Terran.Ghost(u) && u.cloaked))
      addIf(Terran.Irradiate, ours.exists(_.irradiated))
      addIf(Terran.Lockdown, ours.exists(_.lockedDown))
      addIf(Terran.OpticalFlare, ours.exists(_.blind))
      addIf(Terran.SiegeMode, units.exists(Terran.SiegeTankSieged))
      addIf(Terran.SpiderMinePlant, units.exists(Terran.SpiderMine))
      addIf(Terran.Stim, units.exists(_.stimmed))
      addIf(Terran.WraithCloak, units.exists(u => Terran.Wraith(u) && u.cloaked))
      addIf(Zerg.Burrow, units.exists(u => u.burrowed && ! Zerg.Lurker(u)))
      addIf(Zerg.Ensnare, ours.exists(_.ensnared))
      addIf(Zerg.LurkerMorph, units.exists(Zerg.Lurker) || units.exists(Zerg.LurkerEgg))
      addIf(Zerg.Plague, ours.exists(_.plagued))
      addIf(Zerg.SpawnBroodlings, units.exists(Zerg.Broodling))
      addIf(Protoss.Stasis, ours.exists(_.stasised))
    })
  }
}
