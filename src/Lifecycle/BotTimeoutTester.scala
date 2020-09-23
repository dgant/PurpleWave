package Lifecycle

import java.io.{BufferedWriter, File, FileWriter}

import Utilities.ByOption
import bwapi.{DefaultBWListener, Order, Position, UnitType}

import scala.collection.JavaConverters._

class BotTimeoutTester extends DefaultBWListener {

  override def onStart() {
  }

  var targetPosition: Option[Position] = None

  override def onFrame(): Unit = {
    JBWAPIClient.getGame.setLocalSpeed(0)
    JBWAPIClient.getGame.self.getUnits.asScala.foreach(unit => {
      if (unit.getType == UnitType.Terran_Command_Center) {
        if (JBWAPIClient.getGame.self.minerals >= 50) {
          unit.train(UnitType.Terran_SCV)
        } else if (unit.isLifted) {
          val map = JBWAPIClient.getGame.mapFileName
          val hideyspot = if (map.contains("Destination")) {
            new Position(0, 0)
          } else if (map.contains("Heartbreak")) {
            new Position(0, 32 * 22)
          } else if (map.contains("Polaris")) {
            new Position(32 * 43, 0)
          } else if (map.contains("Aztec")) {
            new Position(32 * 127, 32 * 12)
          } else if (map.contains("Longinus")) {
            new Position(32 * 127, 0)
          } else if (map.contains("Breaker")) {
            new Position(0, 32 * 127)
          } else if (map.contains("Empire")) {
            new Position(32 * 64, 32 * 20)
          } else if (map.contains("Fighting")) {
            new Position(32 * 76, 0)
          } else if (map.contains("Python")) {
            new Position(0, 32 * 127)
          } else if (map.contains("Roadkill")) {
            new Position(32 * 98, 0)
          } else {
            new Position(0, 0)
          }
          unit.move(hideyspot)
        } else {
          unit.lift()
        }
      } else {
        val threat = ByOption.minBy(JBWAPIClient.getGame.enemy.getUnits.asScala.filter(u =>
          u.isCompleted
          && u.getType.groundWeapon.damageAmount > 0
          && u.getPosition.getApproxDistance(unit.getPosition)
            < u.getType.groundWeapon.maxRange
            + (if (u.isGatheringMinerals
              || u.isGatheringGas
              || ! Seq(Order.AttackMove, Order.AttackUnit).contains(u.getOrder)) 32 else 128)))(_.getPosition.getApproxDistance(unit.getPosition))
        if (threat.nonEmpty) {
          unit.move(JBWAPIClient.getGame.self.getStartLocation.toPosition)
        } else {
          val target = ByOption.minBy(JBWAPIClient.getGame.enemy.getUnits.asScala.filter(u =>
            u.isVisible
              && ! u.isFlying
              && u.getType.isBuilding))(u =>
                u.getPosition.getApproxDistance(unit.getPosition)
                + (if (Seq(UnitType.Terran_Barracks, UnitType.Protoss_Photon_Cannon, UnitType.Protoss_Gateway, UnitType.Zerg_Spawning_Pool).contains(u.getType)) 0 else 32 * 20))
          if (target.isDefined) {
            targetPosition = Some(JBWAPIClient.getGame.getStartLocations.asScala.map(_.toPosition).minBy(_.getApproxDistance(target.get.getPosition)))
            if (unit.isIdle || unit.getOrderTarget != target.get) {
              target.foreach(unit.attack)
            }
          } else {
            val toScout = targetPosition.orElse(JBWAPIClient.getGame.getStartLocations.asScala.filterNot(JBWAPIClient.getGame.isExplored).headOption.map(_.toPosition))
            toScout.foreach(unit.move)
          }
        }
      }
    })
  }
  
  override def onEnd(isWinner: Boolean): Unit = {
    val file = new File("bwapi-data/write/" + JBWAPIClient.getGame.enemy.getName + ".txt")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(JBWAPIClient.getPerformanceMetrics.toString)
    bw.close()
  }
}
