package Types.Contracts

import Types.Requirements.RequireUnits

import scala.collection.mutable

class ContractUnits(
  val requirements:RequireUnits,
  buyer:Buyer,
  priorityMultiplier: PriorityMultiplier)
    extends Contract(buyer, priorityMultiplier) {
  
  val units:mutable.Set[bwapi.Unit] = mutable.Set.empty
}
