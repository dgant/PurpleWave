package Micro.Behaviors
import Micro.Intentions.Intention
import Startup.With

object BehaviorDragoon extends Behavior {
  
  /*
    A major challenge with microing Dragoons is preventing the Goon Stop bug.
    
    According to JohnJ's exhaustive research, this bug occurs when:
    
    Step 1: Frame 0: Order a Dragoon to move (possibly optional)
    Step 2: Frame N: Order a Dragoon to attack a unit
    Step 3: Frame N+M where M < 10: Order a Dragoon to move in a similar direction as Step 1
    
    I believe there are four ways to avoid Goon Stop:
    1. Refuse to issue Move commands while isAttackFrame == true
    2. Refuse to issue Move commands within 10 frames of issuing an Attack command (probably after latency is accounted for)
    3. Issue a Move command in a different direction than the Dragoon was previously  traveling
    4. Issue a Stop command when we think the Dragoon is stuck
    
      
   */
  
  def execute(intent: Intention) {
    if (With.configuration.enableGoonStopProtection && intent.unit.attackAnimationHappening) {
      return
    }
    
    BehaviorDefault.execute(intent)
  }
}
