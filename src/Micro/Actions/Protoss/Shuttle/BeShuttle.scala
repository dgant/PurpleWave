package Micro.Actions.Protoss.Shuttle

import Debugging.SimpleString
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Protoss.ReloadScarabs
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

import scala.collection.mutable.ArrayBuffer

object BeShuttle extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Shuttle(unit)

  trait QuestType extends SimpleString
  object Deliver  extends QuestType
  object Hover    extends QuestType
  object Pickup   extends QuestType

  case class ShuttleQuest(passenger: FriendlyUnitInfo, quest: QuestType, multiplier: Double, baseCost: Double, perform: () => Unit) {
    val finalCost: Double = baseCost * multiplier
  }

  def supportedPassengers(shuttle: FriendlyUnitInfo): Seq[FriendlyUnitInfo] = {
    val passengers = shuttle.agent.passengersPrioritized
    Maff.orElse(passengers.filter(Protoss.Reaver), passengers).toVector
  }

  def deliveryGoal(shuttle: FriendlyUnitInfo, goal: Pixel): Pixel = {
    val exposed = supportedPassengers(shuttle).filter(p => Protoss.Reaver(p) && !p.loaded)
    Maff.minBy(exposed)(_.pixelDistanceSquared(shuttle))
      .map(p => p.pixel.projectUpTo(goal, 32)).getOrElse(goal)
  }

  protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    if (ShuttleDitchPassengers(shuttle)) return
    if (ShuttleAdoptPassenger(shuttle)) return

    // Emergency pickups
    val supported = supportedPassengers(shuttle)
    val hailers = shuttle.agent.hailersPrioritized.filter(supported.contains)
    val hailersMostUrgent = Maff.orElse(
      hailers.filter(_.doomed),
      hailers.filter(_.likelyDoomed),
      hailers.filter(_.likelyDoomedInFrames < 24),
      hailers.filter(_.matchups.engagedUpon),
      hailers.filter(_.matchups.pixelsToThreatRange.exists(_ < 64)))
    val emergencyHailer = Maff.minBy(hailersMostUrgent)(_.pixelDistanceEdge(shuttle))
    emergencyHailer.foreach(u => {
      pickup(shuttle, u)
      shuttle.agent.act("EmergencyPickup")
      return
    })

    // Ordinary pickups/dropoffs/parking
    val fightRadius = 320
    val passengersFighting = supported.filter(p =>
      p.agent.toAttack.isDefined
      || p.agent.perch.isDefined
      || p.matchups.pixelsToThreatRange.exists(_ < fightRadius)
      || p.matchups.enemies.exists(e => Protoss.Shuttle(e) && e.pixelDistanceEdge(p) < fightRadius))
    val anyoneFighting = passengersFighting.nonEmpty

    val quests = new ArrayBuffer[ShuttleQuest]
    supported.foreach(p => {
      val goal = p.agent.rideGoal.map(g => if (p.loaded) deliveryGoal(shuttle, g) else g)
      val m = ?(passengersFighting.contains(p) || ! anyoneFighting, 1, 10)

      def add(transform: Pixel => ShuttleQuest): Unit = { quests ++= goal.map(transform) }

      // Deliver passenger
      if (p.loaded) {

        add(g => ShuttleQuest(p, Deliver, m, shuttle.pixelDistanceCenter(g), () => deliver(shuttle, g)))

      // Pickup passenger
      } else if (ReloadScarabs.readyForPickup(p) && goal.exists(g => p.framesToTravelTo(g) > 24 + ?(p.canAttack, p.unitClass.cooldownOnDrop, 0)) && p.agent.toAttack.forall(t => Math.min(p.pixelsToGetInRange(t), p.pixelsToGetInRange(t, t.projectFrames(p.unitClass.cooldownOnDrop))) > 32)) {

        add(g => ShuttleQuest(p, Pickup, m, shuttle.pixelDistanceEdge(p), () => pickup(shuttle, p)))

      // Hover over passenger
      } else if (p.agent.toAttack.isDefined || p.agent.perch.isDefined || p.team.exists(_.engagedUpon) || p.matchups.pixelsToThreatRange.exists(_ < 320)) {

        add(g => ShuttleQuest(p, Hover, m, shuttle.pixelDistanceEdge(p) + 160, () => ShuttlePark.parkWithPassengers(shuttle, Seq(p))))

      }
    })

    val quest = Maff.minBy(quests)(_.finalCost)
    quest.foreach(q => {
      q.perform()
      shuttle.agent.act(q.quest.toString)
      return
    })

    ShuttlePark(shuttle)
    ShuttleAwait(shuttle)
  }

  // Pick up a passenger
  //
  private def pickup(shuttle: FriendlyUnitInfo, passenger: FriendlyUnitInfo): Unit = {
    shuttle.agent.decision.set(passenger.pixel)
    Commander.rightClick(shuttle, passenger)
  }

  // Heed a passenger's ride goal
  //
  private def deliver(shuttle: FriendlyUnitInfo, goal: Pixel): Unit = {
    shuttle.agent.decision.set(goal)
    Commander.move(shuttle, allowOvershoot = false)
  }
}
