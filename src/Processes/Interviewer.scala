package Processes

import Startup.With
import Types.Resources.JobDescription

import scala.collection.mutable
import scala.collection.JavaConverters._

object Interviewer {
  def hunt(
    jobDescription: JobDescription,
    hireGreedily:Boolean)
      :Option[Iterable[bwapi.Unit]] = {
    val candidates:mutable.Set[bwapi.Unit] = mutable.Set.empty
    
    //Take unemployed candidates first, then just blindly poach employed candidates like a buffoon
    With.game.self.getUnits.asScala
      .filter(jobDescription.matcher.accept)
      .filter(x => hireGreedily || ! jobDescription.quantity.accept(candidates.size))
      .sortBy(x => With.recruiter.getUnemployed.toSeq.contains(x))
      .foreach(candidate => {
        candidates.add(candidate)
      })
  
    if (jobDescription.quantity.accept(candidates.size)) {
      return Some(candidates)
    }
    
    None
  }
}
