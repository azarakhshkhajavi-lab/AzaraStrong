package com.azarastrong.app

import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.VideoView
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun GuidedWorkoutScreen(session:Session,onExit:()->Unit,onMoveComplete:(Int)->Unit){
 val context=androidx.compose.ui.platform.LocalContext.current
 var voiceReady by remember{mutableStateOf(false)}
 var voiceOn by remember{mutableStateOf(true)}
 val voice=remember{TextToSpeech(context){status->voiceReady=status==TextToSpeech.SUCCESS}}
 DisposableEffect(Unit){onDispose{voice.stop();voice.shutdown()}}
 var index by remember{mutableIntStateOf(0)}
 var count by remember{mutableIntStateOf(0)}
 var running by remember{mutableStateOf(true)}
 var pace by remember{mutableIntStateOf(3)}
 var totalSeconds by remember{mutableIntStateOf(1200)}
 val move=session.moves[index]
 val timed=isTimed(move)
 val target=goalFor(move)

 LaunchedEffect(voiceReady){if(voiceReady)voice.language=Locale.US}
 LaunchedEffect(index,voiceReady,voiceOn){
  if(voiceReady&&voiceOn)voice.speak("Next exercise. "+move.name+". "+move.cue,TextToSpeech.QUEUE_FLUSH,null,"exercise")
 }
 LaunchedEffect(count,voiceReady,voiceOn){
  if(count>0&&voiceReady&&voiceOn)voice.speak(count.toString(),TextToSpeech.QUEUE_ADD,null,"count")
 }

 LaunchedEffect(Unit){while(totalSeconds>0){delay(1000);totalSeconds--}}
 LaunchedEffect(index){count=0;running=true}
 LaunchedEffect(running,index,count,pace){
  if(running&&count<target){
   delay(if(timed)1000 else pace*1000L)
   count++
   if(count>=target){
    running=false;onMoveComplete(index);delay(900)
    if(index<session.moves.lastIndex)index++ else onExit()
   }
  }
 }

 Scaffold(containerColor=Paper,topBar={
  Row(Modifier.fillMaxWidth().background(Ink).padding(10.dp),verticalAlignment=Alignment.CenterVertically){
   IconButton(onClick=onExit){Icon(Icons.Default.Close,"Exit",tint=Color.White)}
   Column(Modifier.weight(1f)){Text(session.title,color=Color.White,fontWeight=FontWeight.Bold);Text("Exercise "+(index+1)+" of "+session.moves.size,color=Color(0xFFD8E2E1),fontSize=12.sp)}
   IconButton(onClick={voiceOn=!voiceOn}){Icon(if(voiceOn)Icons.Default.VolumeUp else Icons.Default.VolumeOff,"Voice",tint=Color.White)}
   Text("%02d:%02d".format(totalSeconds/60,totalSeconds%60),color=Color.White,fontWeight=FontWeight.Bold)
  }
 }){padding->
  Column(Modifier.fillMaxSize().padding(padding).padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){
   LinearProgressIndicator(progress={(index+count.toFloat()/target)/session.moves.size},modifier=Modifier.fillMaxWidth(),color=Coral)
   Spacer(Modifier.height(12.dp))
   Text(move.kind.uppercase(),color=Coral,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.4.sp)
   Text(move.name,fontSize=28.sp,lineHeight=31.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
   Text(move.detail,color=Teal,fontWeight=FontWeight.Bold)
   Spacer(Modifier.height(12.dp))
   ExerciseAnimation(move,running)
   Spacer(Modifier.height(12.dp))
   Text(if(timed)"SECONDS" else "REPETITIONS",color=Color.Gray,fontSize=11.sp,fontWeight=FontWeight.Bold)
   Text(count.toString()+" / "+target,fontSize=44.sp,fontWeight=FontWeight.Black)
   LinearProgressIndicator(progress={count.toFloat()/target},modifier=Modifier.fillMaxWidth().height(8.dp),color=Teal)
   Spacer(Modifier.height(10.dp))
   Text("Muscles working",fontWeight=FontWeight.Bold)
   Row(horizontalArrangement=Arrangement.Center){musclesFor(move).forEach{m->Surface(color=Mint,shape=CircleShape,modifier=Modifier.padding(3.dp)){Text(m,Modifier.padding(horizontal=9.dp,vertical=6.dp),color=Teal,fontSize=11.sp,fontWeight=FontWeight.Bold)}}}
   Card(Modifier.fillMaxWidth().padding(vertical=9.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Text(move.cue,Modifier.padding(12.dp),textAlign=TextAlign.Center,lineHeight=19.sp)}
   if(!timed)Row(verticalAlignment=Alignment.CenterVertically){Text("Pace ",fontWeight=FontWeight.Bold);listOf(4 to "Slow",3 to "Normal",2 to "Fast").forEach{v->FilterChip(pace==v.first,{pace=v.first},label={Text(v.second)},modifier=Modifier.padding(start=4.dp))}}
   Spacer(Modifier.weight(1f))
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
    OutlinedButton({if(count>0)count--},Modifier.weight(1f)){Icon(Icons.Default.Remove,null);Text(" One")}
    Button({running=!running},Modifier.weight(1.2f)){Icon(if(running)Icons.Default.Pause else Icons.Default.PlayArrow,null);Text(if(running)" Pause" else " Continue")}
    OutlinedButton({if(count<target)count++},Modifier.weight(1f)){Icon(Icons.Default.Add,null);Text(" One")}
   }
   Button(onClick={onMoveComplete(index);if(index<session.moves.lastIndex)index++ else onExit()},colors=ButtonDefaults.buttonColors(containerColor=Ink),modifier=Modifier.fillMaxWidth().padding(top=7.dp)){
    Text(if(index==session.moves.lastIndex)"Finish workout" else "Next exercise");Icon(Icons.Default.ArrowForward,null)
   }
  }
 }
}

@Composable
private fun ExerciseAnimation(move:Move,running:Boolean){
 val video=exerciseVideo(move)
 val transition=rememberInfiniteTransition(label="exercise")
 val phase by transition.animateFloat(0f,1f,infiniteRepeatable(tween(if(running)1200 else 100000),RepeatMode.Reverse),label="motion")
 val realistic=realisticExerciseImage(move)
 Card(Modifier.fillMaxWidth().height(235.dp),colors=CardDefaults.cardColors(containerColor=Mint),shape=RoundedCornerShape(22.dp)){
  if(video!=null){
   ExerciseVideo(video,running)
  }else if(realistic!=null){
   Box(Modifier.fillMaxSize()){
    Image(painterResource(realistic),move.name,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
    Row(Modifier.align(Alignment.BottomCenter).padding(9.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){
     MotionLabel("START",phase<.5f)
     MotionLabel("FINISH",phase>=.5f)
    }
   }
  }else Canvas(Modifier.fillMaxSize().padding(16.dp)){drawPerson(move,phase)}
 }
}

@Composable
private fun ExerciseVideo(resourceId:Int,running:Boolean){
 val context=androidx.compose.ui.platform.LocalContext.current
 val videoView=remember(resourceId){
  VideoView(context).apply{
   setBackgroundColor(android.graphics.Color.BLACK)
   setVideoURI(Uri.parse("android.resource://${context.packageName}/$resourceId"))
   setOnPreparedListener{player->
    player.isLooping=true
    player.setVolume(0f,0f)
   }
  }
 }
 AndroidView(
  factory={videoView},
  modifier=Modifier.fillMaxSize(),
  update={view->if(running){if(!view.isPlaying)view.start()}else if(view.isPlaying)view.pause()}
 )
 DisposableEffect(videoView){onDispose{videoView.stopPlayback()}}
}

private fun exerciseVideo(move:Move):Int?=when(move.name){
 "March + arm sweep"->R.raw.exercise_march_arm_sweep
 else->null
}

@Composable private fun MotionLabel(text:String,active:Boolean){
 Surface(color=if(active)Coral else Ink.copy(alpha=.70f),shape=CircleShape){
  Text(text,Modifier.padding(horizontal=13.dp,vertical=6.dp),color=Color.White,fontSize=11.sp,fontWeight=FontWeight.Black)
 }
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

private fun DrawScope.drawPerson(move:Move,phase:Float){
 val n=move.name.lowercase()
 val squat=if("squat" in n||"deadlift" in n)phase*35f else 0f
 val step=if("march" in n||"step" in n||"carry" in n)phase*25f else 0f
 val wide=if(move.kind in listOf("Posture","Back","Shoulders","Chest","Arms"))phase*48f else phase*10f
 val c=Offset(size.width/2,size.height*.43f+squat)
 val stroke=size.width*.035f
 drawCircle(Color(0xFFE3A686),size.width*.065f,Offset(c.x,c.y-size.height*.24f))
 drawLine(Teal,Offset(c.x,c.y-size.height*.16f),Offset(c.x,c.y+size.height*.1f),stroke,StrokeCap.Round)
 drawLine(Coral,Offset(c.x,c.y-size.height*.12f),Offset(c.x-size.width*.18f-wide,c.y),stroke*.7f,StrokeCap.Round)
 drawLine(Coral,Offset(c.x,c.y-size.height*.12f),Offset(c.x+size.width*.18f+wide,c.y),stroke*.7f,StrokeCap.Round)
 drawLine(Ink,Offset(c.x,c.y+size.height*.08f),Offset(c.x-size.width*.12f,c.y+size.height*.32f-step),stroke*.78f,StrokeCap.Round)
 drawLine(Ink,Offset(c.x,c.y+size.height*.08f),Offset(c.x+size.width*.12f,c.y+size.height*.32f+step),stroke*.78f,StrokeCap.Round)
 if("band" in n||"pallof" in n||"pull" in n)drawLine(Coral,Offset(c.x-size.width*.25f-wide,c.y),Offset(c.x+size.width*.25f+wide,c.y),7f,StrokeCap.Round)
 if("dumbbell" in n||"curl" in n||"press" in n||"row" in n||"carry" in n){drawCircle(Ink,13f,Offset(c.x-size.width*.18f-wide,c.y));drawCircle(Ink,13f,Offset(c.x+size.width*.18f+wide,c.y))}
}

private fun isTimed(move:Move)=move.detail.contains("min")||move.detail.contains("sec")
private fun goalFor(move:Move):Int{
 val d=move.detail
 if(d.contains("min"))return(Regex("(\\d+)").find(d)?.value?.toIntOrNull()?:1)*60
 if(d.contains("sec"))return Regex("(\\d+)\\s*sec").find(d)?.groupValues?.get(1)?.toIntOrNull()?:30
 return Regex("×\\s*(\\d+)").find(d)?.groupValues?.get(1)?.toIntOrNull()?:Regex("(\\d+)").find(d)?.value?.toIntOrNull()?:10
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
