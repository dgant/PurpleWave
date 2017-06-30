package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.{Architect, Blueprint, BlueprintMatch, Placement}
import Mathematics.Points.Tile

import scala.collection.mutable

class Groundskeeper {
  
  val updated               : mutable.Set[Blueprint]             = new mutable.HashSet[Blueprint]
  val proposals             : mutable.Set[Blueprint]             = new mutable.HashSet[Blueprint]
  val proposalPlacements    : mutable.Map[Blueprint, Placement]  = new mutable.HashMap[Blueprint, Placement]
  val lastPlacementAttempt  : mutable.Map[Blueprint, Int]        = new mutable.HashMap[Blueprint, Int]
  val requirementMatches    : mutable.Set[BlueprintMatch]         = new mutable.HashSet[BlueprintMatch]
  val proposalsFulfilled    : mutable.Set[Blueprint]             = new mutable.HashSet[Blueprint]
  
  ///////////
  // Tasks //
  ///////////
  
  def update() {
    proposals.diff(updated).foreach(removeDescriptor)
    proposalPlacements.keySet.diff(updated).foreach(removeDescriptor)
    requirementMatches.map(_.requirement).diff(updated).foreach(removeDescriptor)
    updated.clear()
  }
  
  def placeBuildings() {
    var newSearches = 0
    With.architecture.reboot()
    proposalsInUpdateOrder
      .foreach(descriptor =>
        if (With.performance.continueRunning && newSearches < With.configuration.maxGroundskeeperSearches) {
          val placementBefore = proposalPlacements.get(descriptor)
          if (placementBefore.isEmpty) {
            newSearches += 1
          }
          lastPlacementAttempt(descriptor) = With.frame
          val placementAfter = Architect.fulfill(descriptor, placementBefore)
          proposalPlacements.put(descriptor, placementAfter)
          
          // For debugging only!
          val tileBefore  = placementBefore.flatMap(_.tile)
          val tileAfter   = placementAfter.tile
          if (tileBefore != tileAfter) {
            if (tileBefore.isDefined) {
              val putBreakpointHere1 = 12345
            }
            val putBreakpointHere2 = 12345
          }
          if (tileAfter.isEmpty) {
            val putBreakpointHere3 = 12345
          }
        })
  }
  
  private def removeDescriptor(descriptor: Blueprint) {
    proposals.remove(descriptor)
    proposalPlacements.remove(descriptor)
    lastPlacementAttempt.remove(descriptor)
    requirementMatches
      .filter(m => m.requirement == descriptor || m.proposal == descriptor)
      .foreach(requirementMatches.remove)
  }
  
  private def proposalsInUpdateOrder: Iterable[Blueprint] = {
    // Verify existing placements
    // in priority order (top priority first)
    // Then place upcoming proposals
    // ordered by time since last attempt at placement
    // then by priority
    val placed = proposalPlacements.keySet
    val unplaced = proposals.diff(placed)
    val ordered =
      placed
        .toVector
        .sortBy(_.proposer.priority) ++
      unplaced
        .toVector
        .sortBy(_.proposer.priority)
        .sortBy(lastPlacementAttempt.getOrElse(_, 0))
    ordered
  }
  
  //////////////
  // Plan API //
  //////////////
  
  /*
  Propose an idea for building placement.
  Intended for use by Plans that don't actually care when the building gets placed.
  This is just to say "hey, it would be nice if a matching building got built here."
  */
  def propose(proposal: Blueprint) {
    if (proposalsFulfilled.contains(proposal)) return
    flagUpdated(proposal)
    addProposal(proposal)
  }
  
  /*
  Require placement of a building matching the specified criteria.
  Intended for use by Plans that urgently need a building placement.
  If a matching proposal (specified by the above propose()) is available, they'll use that.
  
  So if you want to build a Pylon and don't really care where,
  but someone proposed a specific place for a Pylon,
  use the previously proposed place.
   */
  def require(requirement: Blueprint): Option[Tile] = {
    unfulfillProposalForRequirement(requirement)
    flagUpdated(requirement)
    addRequirement(requirement)
    getTileForRequirement(requirement)
  }
  
  def flagFulfilled(requirement: Blueprint) {
    val proposal = getRepresentativeDescriptorForRequirement(requirement)
    removeDescriptor(requirement)
    removeDescriptor(proposal)
    proposalsFulfilled.add(requirement)
    proposalsFulfilled.add(proposal)
  }
  
  //////////////
  // Internal //
  //////////////
  
  private def flagUpdated(descriptor: Blueprint) {
    updated.add(descriptor)
  }
  
  private def addProposal(proposal: Blueprint) {
    proposals.add(proposal)
  }
  
  private def addRequirement(requirement: Blueprint) {
    val proposal = getRepresentativeDescriptorForRequirement(requirement)
    if (proposal == requirement) {
      addProposal(requirement)
    }
    requirementMatches.add(BlueprintMatch(requirement = requirement, proposal = proposal))
  }
  
  private def getTileForRequirement(requirement: Blueprint): Option[Tile] = {
    val proposal = findProposalAlreadyMatchedWithRequirement(requirement)
    proposal.foreach(flagUpdated)
    proposal.flatMap(proposalPlacements.get).flatMap(_.tile)
  }
  
  private def getRepresentativeDescriptorForRequirement(requirement: Blueprint): Blueprint = {
    findProposalAlreadyMatchedWithRequirement(requirement)
      .orElse(findProposalToMatchWithRequirement(requirement))
      .getOrElse(requirement)
  }
  
  private def findProposalAlreadyMatchedWithRequirement(requirement: Blueprint): Option[Blueprint] = {
    requirementMatches.find(_.requirement == requirement).map(_.proposal)
  }
  
  private def findProposalToMatchWithRequirement(requirement: Blueprint): Option[Blueprint] = {
    proposals
      .diff(requirementMatches.map(_.proposal))
      .find(requirement.fulfilledBy)
  }
  
  private def unfulfillProposalForRequirement(requirement: Blueprint) {
    proposalsFulfilled.remove(getRepresentativeDescriptorForRequirement(requirement))
  }
}
