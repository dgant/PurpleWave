package Tactic.Missions

import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

private[Missions] object DropManifest {
  type Manifest = Map[FriendlyUnitInfo, FriendlyUnitInfo]

  // Match equal-sized Reaver seats before considering filler. Augmenting paths let a
  // Reaver with only one nearby Shuttle displace one that can reach another Shuttle.
  def reavers(transports: Seq[FriendlyUnitInfo], candidates: Seq[FriendlyUnitInfo], maximum: Int): Manifest = {
    val seats = transports.flatMap(t => Vector.fill(t.unitClass.spaceProvided / Protoss.Reaver.spaceRequired)(t)).toVector
    val occupants = mutable.Map.empty[Int, FriendlyUnitInfo]
    val choices = candidates.map(p => p -> seats.indices.filter(i =>
      p.transport.forall(_ == seats(i)) && p.pixelDistanceCenter(seats(i)) < 640).toVector).toMap
    def board(passenger: FriendlyUnitInfo, visited: mutable.Set[Int]): Boolean = {
      val available = choices(passenger).sortBy(i => (
        if (occupants.keys.exists(j => seats(j) == seats(i))) 0 else 1,
        passenger.pixelDistanceSquared(seats(i)), i))
      available.exists(i => visited.add(i) && {
        if (occupants.get(i).forall(board(_, visited))) {
          occupants(i) = passenger
          true
        } else false
      })
    }
    candidates.sortBy(p => (choices(p).size, p.id)).foreach(p => {
      if (occupants.size < maximum) board(p, mutable.Set.empty)
    })
    occupants.map { case (i, p) => p -> seats(i) }.toMap
  }

  // Preserve existing rides, including physical loads, and reserve Reaver seats
  // before filler when a lost transport forces us to rebuild the manifest.
  def fill(transports: Seq[FriendlyUnitInfo], candidates: Seq[FriendlyUnitInfo], initial: Manifest = Map.empty,
           nearby: Boolean = false): Manifest = {
    val result = mutable.Map.empty[FriendlyUnitInfo, FriendlyUnitInfo]
    val room = mutable.Map(transports.map(t => t -> t.unitClass.spaceProvided): _*)
    candidates.zipWithIndex.sortBy { case (p, i) => (if (Protoss.Reaver(p)) 0 else 1, if (p.loaded) 0 else 1, i) }.foreach { case (p, _) =>
      val choices = transports.filter(t =>
        p.transport.forall(_ == t) && room(t) >= p.unitClass.spaceRequired && (!nearby || p.pixelDistanceCenter(t) < 640))
      choices.sortBy(t => (if (initial.get(p).contains(t)) 0 else 1, p.pixelDistanceSquared(t), t.id)).headOption.foreach(t => {
        result(p) = t
        room(t) -= p.unitClass.spaceRequired
      })
    }
    result.toMap
  }
}
