package Planning.Composition.ResourceLocks

import Planning.Plan

trait ResourceLock {
  
  def isComplete:Boolean
  def acquire(plan:Plan)
  def release()
}
