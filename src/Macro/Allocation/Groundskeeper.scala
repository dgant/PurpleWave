package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.{Blueprint, BlueprintMatch, Placement}
import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Groundskeeper {
  
  val updated               : mutable.Set[Blueprint]             = new mutable.HashSet[Blueprint]
  val proposals             : mutable.Set[Blueprint]             = new mutable.HashSet[Blueprint]
  val proposalPlacements    : mutable.Map[Blueprint, Placement]  = new mutable.HashMap[Blueprint, Placement]
  val requirementMatches    : mutable.Set[BlueprintMatch]        = new mutable.HashSet[BlueprintMatch]
  val proposalsFulfilled    : mutable.Map[Blueprint, UnitInfo]   = new mutable.HashMap[Blueprint, UnitInfo]
  val placementArchive      : mutable.Map[Blueprint, Placement]  = new mutable.HashMap[Blueprint, Placement]
  
  private var lastUrgentBuildingPlacement = -24 * 60 * 60
  
  private var nextId: Int = 0
  
  ///////////
  // Tasks //
  ///////////
  
  def update() {
    proposalsFulfilled.foreach(pair => if ( ! pair._2.alive) flagUnfulfilled(pair._1))
    proposals.foreach(proposal => if ( ! updated.contains(proposal)) removeBlueprint(proposal))
    proposalPlacements.foreach(pair => if ( ! updated.contains(pair._1)) removeBlueprint(pair._1))
    requirementMatches.foreach(m => if ( ! updated.contains(m.requirement)) removeBlueprint(m.requirement))
    updated.clear()
  }
  
  def updatePlacement(blueprint: Blueprint, placement: Placement) {
    proposalPlacements.put(blueprint, placement)
    placementArchive.put(blueprint, placement)
  }
  
  // CIG 2017 HACK: This needs to match the sort below (priority -> id)
  // Ideally we'd represent this in one Ordering that could be used by both queues,
  // but one is BlueprintMatch and the other is Blueprint
  //
  private val blueprintMatchOrdering = Ordering.by { b : BlueprintMatch  => (b.proposal.proposer.priority, b.proposal.id) }
  
  def proposalQueue: Iterable[Blueprint] = {
    val ordered = requirementMatches
      .toVector
      .filter(_.requirement.proposer.isPrioritized)
      .sorted(blueprintMatchOrdering)
      .map(_.proposal)
      .take(With.configuration.buildingPlacementMaximumQueue)
    
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
    tagWithId(proposal)
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
    tagWithId(requirement)
    flagUpdated(requirement)
    addRequirement(requirement)
    getTileForRequirement(requirement)
  }
  
  /*
    Some jankiness from building placement is tolerable later in the game.
    But if it wrecks our early game build we just plain lose.
    Example: Failing to place the Forge in an FFE vs. a 4-pool until it's too late
  */
  def demand(requirement: Blueprint): Option[Tile] = {
    require(requirement).orElse({
      requestUrgentPlacement(requirement)
      require(requirement)
    })
  }
  
  def tagWithId(blueprint: Blueprint) {
    if (blueprint.id.isEmpty) {
      blueprint.id = Some(nextId)
      nextId += 1
    }
  }
  
  def flagFulfilled(requirement: Blueprint, fulfillingUnit: UnitInfo) {
    // 2-24-18 made a change here that may break everything because building placement is a disaster
    if (proposalsFulfilled.exists(_._2 == fulfillingUnit)) {
      return
    }
    val proposal = getRepresentativeBlueprintForRequirement(requirement)
    removeBlueprint(requirement)
    proposalsFulfilled.put(proposal,    fulfillingUnit)
    proposalsFulfilled.put(requirement, fulfillingUnit)
    requirementMatches --= requirementMatches.find(x => x.proposal == proposal && x.requirement == requirement)
  }
  
  def flagUnfulfilled(requirement: Blueprint) {
    val proposal = getRepresentativeBlueprintForRequirement(requirement)
    addProposal(proposal)
    addRequirement(requirement)
    proposalsFulfilled.remove(proposal)
    proposalsFulfilled.remove(requirement)
  }
  
  //////////////
  // Internal //
  //////////////
  
  private def flagUpdated(blueprint: Blueprint) {
    updated.add(blueprint)
  }
  
  private def addProposal(proposal: Blueprint) {
    proposals.add(proposal)
  }
  
  private def removeBlueprint(blueprint: Blueprint) {
    proposals.remove(blueprint)
    proposalPlacements.remove(blueprint)
    requirementMatches
      .filter(m => m.requirement == blueprint || m.proposal == blueprint)
      .foreach(requirementMatches.remove)
  }
  
  private def addRequirement(requirement: Blueprint) {
    val proposal = getRepresentativeBlueprintForRequirement(requirement)
    if (proposal == requirement) {
      addProposal(requirement)
    }
    requirementMatches.add(BlueprintMatch(requirement = requirement, proposal = proposal))
  }
  
  private def getTileForRequirement(requirement: Blueprint): Option[Tile] = {
    val proposal = findProposalAlreadyMatchedWithRequirement(requirement)
    proposal.foreach(flagUpdated)
    var output = getTileForProposal(proposal)
    output
  }
  
  private def requestUrgentPlacement(requirement: Blueprint) {
    if (With.configuration.urgentBuildingPlacement
    && With.frame < With.configuration.urgentBuildingPlacementCutoffFrames
    && ! With.performance.danger
    && With.framesSince(lastUrgentBuildingPlacement) > With.configuration.urgentBuildingPlacementCooldown) {
      lastUrgentBuildingPlacement = With.frame
      With.placement.run(runToCompletionEvenIfItCostsUsAFrame = true)
    }
  }
  
  private def getTileForProposal(proposal: Option[Blueprint]): Option[Tile] = {
    proposal.flatMap(proposalPlacements.get).flatMap(_.tile)
  }
  
  private def getRepresentativeBlueprintForRequirement(requirement: Blueprint): Blueprint = {
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
      .diff(proposalsFulfilled.keySet)
      .toVector
      // CIG 2017 HACK: This needs to match the sort above (priority -> id)
      // Ideally we'd represent this in one Ordering that could be used by both queues,
      // but one is BlueprintMatch and the other is Blueprint.
      //
      .sortBy(p => (p.proposer.priority, p.id))
      .find(requirement.fulfilledBy)
  }
  
  private def unfulfillProposalForRequirement(requirement: Blueprint) {
    proposalsFulfilled.remove(getRepresentativeBlueprintForRequirement(requirement))
  }
}
