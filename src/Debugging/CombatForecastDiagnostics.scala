package Debugging

import Information.Battles.Types.Battle
import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactic.Missions.MissionDrop

// Opt-in gameplay diagnostics; per-unit logging is too expensive for normal tournament games.
object CombatForecastDiagnostics {
  private var lastCycle = -1000
  private val lastUnit = scala.collection.mutable.Map.empty[Int, Int]
  private def ids(units: Iterable[UnitInfo]): String = units.map(_.id).mkString(",")
  private def parentId(b: Battle): Int = System.identityHashCode(b)
  private def group(u: UnitInfo): String = u.friendly.flatMap(_.squad).filter(_.fightsIndependently).map(_.toString).getOrElse("Army")
  def cycle(): Unit = {
    if ( ! With.configuration.logCombatForecasts) return
    if (With.frame - lastCycle < 24) return
    val mixed = With.battles.local.filter(_.combatForecasts.size > 1)
    if (mixed.isEmpty) return
    lastCycle = With.frame
    mixed.foreach { parent =>
      parent.combatForecasts.foreach { b =>
        val name = b.us.units.headOption.map(group).getOrElse("Army")
        val j = b.judgement.get
        With.logger.debug(s"FORECAST parent=${parentId(parent)} group=${name} created=${b.frameCreated} friends=${ids(b.us.units)} enemies=${ids(b.enemy.units)} ground=${j.shouldFightGround} air=${j.shouldFightAir} score=${j.scoreTotal} target=${j.scoreTarget} confidence=${j.confidence11Ground} frames=${b.simulationFrames} stale=${b.us.units.exists(u => !u.combatForecast.contains(b))}")
        b.us.units.foreach { u =>
          val state = u.friendly.flatMap(_.squad).collect { case m: MissionDrop => m.state.toString }.getOrElse("Army")
          With.logger.debug(s"FORECAST_MEMBER parent=${parentId(parent)} group=${name} id=${u.id} kind=${u.unitClass} loaded=${u.loaded} x=${u.pixel.x} y=${u.pixel.y} state=$state")
        }
      }
    }
  }
  def decision(u: FriendlyUnitInfo): Unit = synchronized {
    if ( ! With.configuration.logCombatForecasts) return
    if (With.frame - lastUnit.getOrElse(u.id, -1000) < 24) return
    u.battle.filter(_.combatForecasts.size > 1).foreach { parent =>
      lastUnit(u.id) = With.frame
      val j = u.combatJudgment
      With.logger.debug(s"FORECAST_DECISION parent=${parentId(parent)} id=${u.id} group=${group(u)} ground=${j.map(_.shouldFightGround)} actual=${u.agent.shouldFight} reason=${u.agent.fightReason} loaded=${u.loaded} x=${u.pixel.x} y=${u.pixel.y} order=${u.order} target=${u.orderTarget.map(_.id)}")
    }
  }
}
