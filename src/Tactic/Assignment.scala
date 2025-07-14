package Tactic

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Tactic.Squads.Squad
import Utilities.?

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Assignment {

  // Let units pick the squad they can best serve
  def unitsPick(
     freelancers  : mutable.Buffer[FriendlyUnitInfo],
     squads       : Seq[Squad],
     minimumValue : Double                                = Double.NegativeInfinity,
     filter       : (FriendlyUnitInfo, Squad) => Boolean  = (f, s) => true): Unit = {
    var i = 0
    while (i < freelancers.length) {
      val freelancer      = freelancers(i)
      val squadsEligible  = squads.filter(squad => filter(freelancer, squad) && squad.candidateValue(freelancer) > minimumValue)
      val bestSquad       = Maff.minBy(squadsEligible)(squad => freelancer.pixelDistanceTravelling(squad.vicinity) + ?(freelancer.squad.contains(squad), 0, 320))
      if (bestSquad.isDefined) {
        bestSquad.get.addUnit(freelancers.remove(i))
        With.recruiter.lockTo(bestSquad.get.lock, freelancer)
      } else {
        i += 1
      }
    }
  }

  // Let squads pick the freelancers they need
  def squadsPick(
      freelancers   : mutable.Buffer[FriendlyUnitInfo],
      squads        : Seq[Squad],
      minimumValue  : Double                                = Double.NegativeInfinity,
      filter        : (FriendlyUnitInfo, Squad) => Boolean  = (f, s) => true): Unit = {
    val freelancersSorted     = new ArrayBuffer[FriendlyUnitInfo]
    val freelancersAvailable  = new mutable.HashSet[FriendlyUnitInfo]
    freelancersSorted     ++= freelancers
    freelancersAvailable  ++= freelancers
    squads.foreach(squad => {
      Maff.sortStablyInPlaceBy(freelancersSorted)(_.framesToTravelTo(squad.vicinity))
      var hired: Option[FriendlyUnitInfo] = None
      do {
        hired = freelancersSorted.find(u => freelancersAvailable.contains(u) && squad.candidateValue(u) > minimumValue && filter(u, squad))
        hired.foreach(h => {
          freelancersAvailable -= h
          freelancers -= h
          squad.addUnit(h)
          With.recruiter.lockTo(squad.lock, h)
        })
      } while (hired.isDefined)
    })
  }
}
