package com.azarastrong.app

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.*
import java.util.Locale

private class VoiceCoach(context:Context){
 var ready=false
 private val tts=TextToSpeech(context){ready=it==TextToSpeech.SUCCESS}
 fun say(text:String,enabled:Boolean){
  if(ready&&enabled){tts.language=Locale.US;tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"cue")}
 }
 suspend fun explain(text:String,enabled:Boolean){
  if(!enabled)return
  withTimeoutOrNull(2000){while(!ready)delay(50)}
  if(!ready)return
  val finished=CompletableDeferred<Unit>()
  val id="intro-"+System.nanoTime()
  tts.setOnUtteranceProgressListener(object:UtteranceProgressListener(){
   override fun onStart(utteranceId:String?){}
   override fun onDone(utteranceId:String?){if(utteranceId==id)finished.complete(Unit)}
   @Deprecated("Platform callback") override fun onError(utteranceId:String?){if(utteranceId==id)finished.complete(Unit)}
  })
  tts.language=Locale.US
  tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,id)
  withTimeoutOrNull(15000){finished.await()}
 }
 fun stop(){tts.stop()}
 fun close(){tts.stop();tts.shutdown()}
}
@Composable
fun GuidedWorkoutScreen(session:Session,onExit:()->Unit,onMoveComplete:(Int)->Unit){
 val context=androidx.compose.ui.platform.LocalContext.current
 val view=androidx.compose.ui.platform.LocalView.current
 val coach=remember{VoiceCoach(context)}
 val steps=remember(session){workoutSteps(session)}
 var stepIndex by remember{mutableIntStateOf(0)}
 var visit by remember{mutableIntStateOf(0)}
 var count by remember{mutableIntStateOf(0)}
 var remaining by remember{mutableIntStateOf(0)}
 var phase by remember{mutableStateOf("intro")}
 var paused by remember{mutableStateOf(false)}
 var voiceOn by remember{mutableStateOf(true)}
 var prompt by remember{mutableStateOf("Get ready")}
 var finished by remember{mutableStateOf(false)}
 val step=steps[stepIndex]
 val move=session.moves[step.moveIndex]
 fun goToExercise(index:Int){
  coach.stop()
  stepIndex=steps.indexOfFirst{it.moveIndex==index}.coerceAtLeast(0)
  visit++
 }
 DisposableEffect(coach,view){
  val previous=view.keepScreenOn;view.keepScreenOn=true
  onDispose{view.keepScreenOn=previous;coach.close()}
 }
 val lifecycleOwner=androidx.lifecycle.compose.LocalLifecycleOwner.current
 DisposableEffect(lifecycleOwner){
  val observer=androidx.lifecycle.LifecycleEventObserver{_,event->
   if(event==androidx.lifecycle.Lifecycle.Event.ON_STOP){paused=true;coach.stop()}
  }
  lifecycleOwner.lifecycle.addObserver(observer)
  onDispose{lifecycleOwner.lifecycle.removeObserver(observer)}
 }
 androidx.activity.compose.BackHandler{
  if(step.moveIndex>0)goToExercise(step.moveIndex-1)else onExit()
 }
 suspend fun waitUnpaused(){while(paused)delay(100)}
 suspend fun tick(){
  var elapsed=0L
  while(elapsed<1000L){waitUnpaused();delay(50);if(!paused)elapsed+=50}
 }
 LaunchedEffect(stepIndex,visit){
  coach.stop();finished=false;paused=false;phase="intro";count=0;remaining=step.dose.target
  prompt=move.name
  val side=if(step.dose.sides==2)". Side "+step.side else ""
  coach.explain(move.name+". Set "+step.set+" of "+step.dose.sets+side+". "+move.cue,voiceOn)
  waitUnpaused()
  phase="ready"
  for(n in 3 downTo 1){waitUnpaused();prompt=n.toString();coach.say(prompt,voiceOn);tick()}
  waitUnpaused();coach.say("Start",voiceOn);prompt="Start";phase="active"
  if(step.dose.timed){
   while(remaining>0){
    waitUnpaused()
    endingCue(remaining)?.let{coach.say(it,voiceOn)}
    tick();remaining--
   }
  }else if(exerciseVideo(move)==null){
   while(count<step.dose.target){
    repeat(3){tick()}
    waitUnpaused();count++;coach.say(count.toString(),voiceOn)
   }
  }else{
   while(count<step.dose.target){delay(50)}
  }
  phase="complete";coach.say("Stop",voiceOn);prompt="Well done"
  val lastForMove=stepIndex==steps.lastIndex||steps[stepIndex+1].moveIndex!=step.moveIndex
  if(lastForMove)onMoveComplete(step.moveIndex)
  delay(1500)
  if(stepIndex<steps.lastIndex){
   phase="rest"
   val next=steps[stepIndex+1]
   coach.explain(if(next.moveIndex==step.moveIndex&&next.side!=step.side)"Switch sides." else "Take a short rest.",voiceOn)
   repeat(10){prompt="Rest · "+(10-it)+" sec";tick()}
   stepIndex++
  }else{
   finished=true;coach.explain("Workout complete. Well done.",voiceOn)
  }
 }
 val active=phase=="active"&&!paused&&!finished
 Scaffold(containerColor=Paper,topBar={
  Row(Modifier.statusBarsPadding().fillMaxWidth().padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
   IconButton(onClick={coach.stop();onExit()}){Icon(Icons.Default.Close,"Back to day")}
   Column(Modifier.weight(1f)){
    Text(session.title,fontWeight=FontWeight.Bold,fontSize=14.sp)
    Text("Exercise "+(step.moveIndex+1)+" of "+session.moves.size,color=Teal,fontSize=12.sp)
   }
   IconButton(onClick={voiceOn=!voiceOn;if(!voiceOn)coach.stop()}){
    Icon(if(voiceOn)Icons.Default.VolumeUp else Icons.Default.VolumeOff,if(voiceOn)"Mute voice" else "Enable voice",tint=Teal)
   }
  }
 },bottomBar={
  Surface(color=Paper,shadowElevation=8.dp){
   if(finished)Button(onClick=onExit,modifier=Modifier.navigationBarsPadding().padding(18.dp).fillMaxWidth().height(56.dp)){Text("Back to day")}
   else Row(Modifier.navigationBarsPadding().padding(12.dp).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
    OutlinedButton(onClick={goToExercise(step.moveIndex-1)},enabled=step.moveIndex>0,modifier=Modifier.weight(1f).height(56.dp),contentPadding=PaddingValues(5.dp)){
     Icon(Icons.Default.SkipPrevious,null,Modifier.size(20.dp));Text("Previous",fontSize=12.sp)
    }
    Button(onClick={paused=!paused;if(paused)coach.stop()},modifier=Modifier.weight(1.15f).height(56.dp),contentPadding=PaddingValues(5.dp)){
     Icon(if(paused)Icons.Default.PlayArrow else Icons.Default.Pause,null,Modifier.size(20.dp));Text(if(paused)"Resume" else "Pause",fontSize=13.sp)
    }
    OutlinedButton(onClick={
     if(step.moveIndex<session.moves.lastIndex)goToExercise(step.moveIndex+1)else onExit()
    },modifier=Modifier.weight(1f).height(56.dp),contentPadding=PaddingValues(5.dp)){
     Text(if(step.moveIndex==session.moves.lastIndex)"Exit" else "Next",fontSize=12.sp);Icon(Icons.Default.SkipNext,null,Modifier.size(20.dp))
    }
   }
  }
 }){padding->
  Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){
   LinearProgressIndicator(progress={stepIndex.toFloat()/steps.size},modifier=Modifier.fillMaxWidth(),color=Teal,trackColor=Mint)
   Spacer(Modifier.height(20.dp))
   Text(move.kind.uppercase(),color=Teal,fontSize=11.sp,letterSpacing=2.sp,fontWeight=FontWeight.Bold)
   Text(move.name,fontSize=29.sp,lineHeight=34.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
   Spacer(Modifier.height(8.dp))
   Text("Set "+step.set+" / "+step.dose.sets+(if(step.dose.sides==2)"  ·  Side "+step.side+" / 2" else ""),color=Teal)
   Spacer(Modifier.height(18.dp))
   key(visit,stepIndex){
    Box(Modifier.fillMaxWidth().aspectRatio(16f/9f).background(Mint,RoundedCornerShape(20.dp))){
     val video=exerciseVideo(move)
     if(video!=null)ExerciseVideo(video,active){position->
      if(active&&!step.dose.timed&&count<step.dose.target){
       count++;coach.say(count.toString(),voiceOn)
      }
     }
     else{
      val still=realisticExerciseImage(move)
      if(still!=null)Image(painterResource(still),move.name,Modifier.fillMaxSize(),contentScale=ContentScale.Fit)
      else Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally){
       Icon(Icons.Default.FitnessCenter,null,tint=Teal,modifier=Modifier.size(48.dp))
       Text("Follow the spoken form cue",Modifier.padding(top=12.dp),color=Ink)
       Text("Video not yet available",color=Teal,fontSize=12.sp)
      }
     }
    }
   }
   Spacer(Modifier.height(22.dp))
   val headline=when{
    finished->"Workout complete"
    paused->"Paused"
    phase=="active"&&step.dose.timed->"%02d:%02d".format(remaining/60,remaining%60)
    phase=="active"->count.toString()+" / "+step.dose.target
    else->prompt
   }
   Text(headline,fontSize=if(phase=="active"||phase=="ready")46.sp else 26.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
   if(phase=="active")Text(if(step.dose.timed)"SECONDS REMAINING" else "REPETITIONS",fontSize=11.sp,letterSpacing=1.sp,color=Teal)
   Spacer(Modifier.height(18.dp))
   Surface(color=Color.White,shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth()){
    Column(Modifier.padding(18.dp)){
     Text("FORM FOCUS",fontSize=10.sp,letterSpacing=2.sp,color=Teal,fontWeight=FontWeight.Bold)
     Text(move.cue,Modifier.padding(top=8.dp),fontSize=16.sp,lineHeight=23.sp)
     Text("Muscles · "+musclesFor(move).joinToString(" · "),Modifier.padding(top=12.dp),fontSize=12.sp,color=Teal)
    }
   }
   Spacer(Modifier.height(12.dp))
   Text("Move within a comfortable range. Stop if you feel pain.",fontSize=11.sp,color=Teal,textAlign=TextAlign.Center)
  }
 }
}
@Composable
private fun ExerciseVideo(resourceId:Int,running:Boolean,onRep:(Long)->Unit){
 val context=androidx.compose.ui.platform.LocalContext.current
 val currentRep by rememberUpdatedState(onRep)
 var error by remember{mutableStateOf(false)}
 val player=remember(resourceId){
  ExoPlayer.Builder(context).build().apply{
   setMediaItem(MediaItem.fromUri("android.resource://"+context.packageName+"/"+resourceId))
   repeatMode=Player.REPEAT_MODE_ONE;volume=0f
   addListener(object:Player.Listener{
    override fun onPlayerError(e:androidx.media3.common.PlaybackException){error=true}
   })
   prepare()
  }
 }
 LaunchedEffect(player,running){player.playWhenReady=running}
 LaunchedEffect(player){
  var previous=-1L
  val markers=when(resourceId){
   R.raw.exercise_band_pull_apart->listOf(4000L,9000L)
   R.raw.exercise_one_arm_row->listOf(3000L,8500L)
   R.raw.exercise_wall_slide->listOf(4700L,9300L)
   R.raw.exercise_pallof_press->listOf(3300L)
   else->emptyList()
  }
  while(true){
   if(player.isPlaying){
    val now=player.currentPosition
    if(now<previous)previous=-1L
    markers.filter{it>previous&&it<=now}.forEach{currentRep(it)}
    previous=now
   }
   delay(40)
  }
 }
 DisposableEffect(player){onDispose{player.release()}}
 if(error)Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("Video unavailable. Follow the form cue.",textAlign=TextAlign.Center)}
 else AndroidView(factory={ctx->
  (android.view.LayoutInflater.from(ctx).inflate(R.layout.exercise_player,null,false) as PlayerView).apply{useController=false;this.player=player}
 },update={it.player=player},modifier=Modifier.fillMaxSize())
}

private fun exerciseVideo(move:Move):Int?=when(move.name){
 "March + arm sweep"->R.raw.exercise_march_arm_sweep
 "Band pull-apart"->R.raw.exercise_band_pull_apart
 "One-arm dumbbell row"->R.raw.exercise_one_arm_row
 "Wall slides"->R.raw.exercise_wall_slide
 "Standing Pallof press"->R.raw.exercise_pallof_press
 "Suitcase march"->R.raw.exercise_suitcase_march
 else->null
}


private fun realisticExerciseImage(move:Move):Int?=when(move.name){
 "March + arm sweep"->R.drawable.exercise_march_arm_sweep
 "Band pull-apart"->R.drawable.exercise_band_pull_apart
 "One-arm dumbbell row"->R.drawable.exercise_one_arm_row
 "Wall slides"->R.drawable.exercise_wall_slide
 "Standing Pallof press"->R.drawable.exercise_pallof_press
 "Suitcase march"->R.drawable.exercise_suitcase_march
 else->null
}


private fun musclesFor(move:Move):List<String>{
 val n=move.name.lowercase()
 return when{
  move.kind=="Glutes"||"squat" in n||"deadlift" in n->listOf("Glutes","Hips","Thighs")
  move.kind in listOf("Posture","Back")->listOf("Upper back","Rear shoulders","Core")
  move.kind=="Core"->listOf("Deep core","Obliques","Back")
  move.kind=="Chest"->listOf("Chest","Shoulders","Triceps")
  move.kind=="Shoulders"->listOf("Shoulders","Upper back","Triceps")
  move.kind=="Arms"->listOf("Biceps","Forearms","Shoulders")
  else->listOf("Heart","Core","Legs")
 }
}

