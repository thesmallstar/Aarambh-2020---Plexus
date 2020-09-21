<?php 
$i =  count($allPosts)-1;
$m=1;
?>
<h2>Submitted Emergencies</h2>

@for($j = $i ; $j>=0; $j--)
  <h2>{{$m}}.{{ $allPosts[$j]->title }}</h2>
 <p>Submitted by: {{$allPosts[$j]->name }} ({{$allPosts[$j]->email }})</p>
<br><img src="{{'uploads/gallery/'.$allPosts[$j]->img}}" width=300px; height=300px;>
<br>
 <br>  {{ $allPosts[$j]->description }}
    

 <?php $m++ ?>
@endfor