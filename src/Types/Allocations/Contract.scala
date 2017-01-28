package Types.Allocations

import Types.Resources.JobDescription

import scala.collection.mutable

class Contract(val jobDescription:JobDescription) {
  val employees:mutable.Set[bwapi.Unit] = mutable.Set.empty
}
