package Information.Battles.Estimations.Simulation

import Information.Battles.Estimations.ReportCard
import Mathematics.Points.Pixel
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

case class Simulacrum(simulation: Simulation, unit: UnitInfo) {
  
  lazy val targetQueue: mutable.PriorityQueue[Simulacrum] = (
    new mutable.PriorityQueue[Simulacrum]()
      (Ordering.by( - _.pixel.pixelDistanceFast(pixel)))
      ++ unit.matchups.targets.map(simulation.simulacra))
  
  var hitPoints         : Int                 = unit.totalHealth
  var cooldown          : Int                 = unit.cooldownLeft
  var pixel             : Pixel               = unit.pixelCenter
  var dead              : Boolean             = false
  var target            : Option[Simulacrum]  = None
  var atTarget          : Boolean             = false
  var killed            : Int                 = 0
  var damageDealt       : Double              = 0.0
  var damageReceived    : Double              = 0.0
  var valueDealt        : Double              = 0.0
  var valueReceived     : Double              = 0.0
  var valuePerDamage    : Double              = MicroValue.valuePerDamage(unit)
  
  def checkDeath() {
    dead = dead || hitPoints <= 0
  }
  
  def step() {
    if (dead) {}
    else if (cooldown > 0) {
      simulation.updated = true
      cooldown -= 1
    }
    else {
      acquireTarget()
      if (target.exists( ! _.dead)) {
        simulation.updated = true
        if (atTarget) {
          strikeTarget()
        }
        else {
          chaseTarget()
        }
      }
    }
  }
  
  def acquireTarget() {
    while (target.forall(_.dead) && targetQueue.nonEmpty) {
      target = Some(targetQueue.dequeue())
      atTarget = false
    }
  }
  
  def chaseTarget() {
    cooldown = unit.framesToTravelPixels(pixel.pixelDistanceFast(target.get.pixel))
    pixel = target.get.pixel //Consider that this puts us closer to our opponent for this whole time!
  }
  
  def strikeTarget() {
    val victim            = target.get
    val damage            = Math.min(target.get.hitPoints, unit.damageAgainst(victim.unit))
    val value             = damage * victim.valuePerDamage
    cooldown              = unit.cooldownMaxAgainst(victim.unit)
    damageDealt           += damage
    valueDealt            += value
    victim.hitPoints      -= damage
    victim.damageReceived += damage
    victim.valueReceived  += value
  }
  
  def reportCard: ReportCard = ReportCard(
    valueDealt      = valueDealt,
    valueReceived   = valueReceived,
    damageDealt     = damageDealt,
    damageReceived  = damageReceived,
    dead            = dead,
    killed          = killed
  )
}
