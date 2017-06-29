package Macro.Allocation

import Lifecycle.With
import Macro.Architecture.{BuildingDescriptor, Placement}
import Mathematics.Points.Tile

import scala.collection.mutable

class Groundskeeper {
  
  case class RequirementMatch(requirement: BuildingDescriptor, proposal: BuildingDescriptor)
  
  val updated             : mutable.Set[BuildingDescriptor]             = new mutable.HashSet[BuildingDescriptor]
  val proposals           : mutable.Set[BuildingDescriptor]             = new mutable.HashSet[BuildingDescriptor]
  val proposalPlacements  : mutable.Map[BuildingDescriptor, Placement]  = new mutable.HashMap[BuildingDescriptor, Placement]
  val requirementMatches  : mutable.Set[RequirementMatch]               = new mutable.HashSet[RequirementMatch]
  
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
    With.architect.reboot()
    Vector(proposalPlacements.keys, proposals.diff(proposalPlacements.keySet))
      .foreach(descriptors => sortByPriority(descriptors)
        .take(With.configuration.buildingPlacements) //Let's just do a few per run so we run frequently in smaller batches
        .foreach(descriptor =>
        if (With.performance.continueRunning) {
          val placement = With.architect.fulfill(descriptor, proposalPlacements.get(descriptor))
          if (placement.tile.isDefined) {
            proposalPlacements.put(descriptor, placement)
          }
          else {
            proposalPlacements.remove(descriptor)
          }
        }))
  }
  
  private def removeDescriptor(descriptor: BuildingDescriptor) {
    proposals.remove(descriptor)
    proposalPlacements.remove(descriptor)
    requirementMatches
      .filter(m => m.requirement == descriptor || m.proposal == descriptor)
      .foreach(requirementMatches.remove)
  }
  
  //////////////
  // Plan API //
  //////////////
  
  def propose(proposal: BuildingDescriptor) {
    flagUpdated(proposal)
    addProposal(proposal)
  }
  
  def require(requirement: BuildingDescriptor): Option[Tile] = {
    flagUpdated(requirement)
    addRequirement(requirement)
    getTileForRequirement(requirement)
  }
  
  ///////////////////////
  // Visualization API //
  ///////////////////////
  
  def sortByPriority(descriptors: Iterable[BuildingDescriptor] ): Iterable[BuildingDescriptor] = {
    descriptors.toVector.sortBy(proposal => With.prioritizer.getPriority(proposal.proposer))
  }
  
  //////////////
  // Internal //
  //////////////
  
  private def flagUpdated(descriptor: BuildingDescriptor) {
    updated.add(descriptor)
  }
  private def addProposal(proposal: BuildingDescriptor) {
    proposals.add(proposal)
  }
  
  private def addRequirement(requirement: BuildingDescriptor) {
    val proposal = proposals
      .diff(requirementMatches.map(_.proposal))
      .find(requirement.fulfilledBy)
      .getOrElse(requirement)
    if (proposal == requirement) {
      addProposal(requirement)
    }
    requirementMatches.add(RequirementMatch(requirement = requirement, proposal = proposal))
  }
  
  private def getTileForRequirement(requirement: BuildingDescriptor): Option[Tile] = {
    proposalPlacements
      .get(getProposalForRequirement(requirement))
      .flatMap(_.tile)
  }
  
  private def getProposalForRequirement(requirement: BuildingDescriptor): BuildingDescriptor = {
    requirementMatches.find(_.requirement == requirement).get.proposal
  }
}
