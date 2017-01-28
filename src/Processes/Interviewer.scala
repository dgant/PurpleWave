package Processes

import Startup.With
import Types.Resources.JobDescription

import scala.collection.mutable

object Interviewer {
  def hunt(
    jobDescription: JobDescription,
    hireGreedily:Boolean)
      :Option[Iterable[bwapi.Unit]] = {
    val candidates:mutable.Set[bwapi.Unit] = mutable.Set.empty
    
    //First, take unemployed candidates
    //TODO: poach employed candidates
    With.recruiter.getUnemployed
      .filter(jobDescription.matcher.accept)
      .foreach(candidate => {
        
        //So, I think this is going to be slow because it will match on every unit every time,
        //even if the job description quantity has been fulfilled.
        //But if filtering is lazy-evaluated, we could insteadput the accept() as a filter
        //
        if (hireGreedily || ! jobDescription.quantity.accept(candidates.size)) {
          candidates.add(candidate)
        }
      })
  
    if (jobDescription.quantity.accept(candidates.size)) {
      return Some(candidates)
    }
    
    None
  }
}
