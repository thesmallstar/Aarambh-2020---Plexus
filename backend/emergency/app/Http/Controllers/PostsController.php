<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use \App\post;

class PostsController extends Controller
{
          public function addPost(Request $request){
                    $newPost = new post();
                    //return $request;
                    
                    $filename =time().rand(1,10000).'.jpeg';
                    $file = $request["image"];
                    $file->move('uploads/gallery/',$filename);
                    $newPost->name = $request["name"];  
                    $newPost->email = $request["email"];  
                    $newPost->title = $request["title"]; 
                    $newPost->img =  $filename;
                    $newPost->description = $request["description"];
                    $newPost->save();
                    return "success";
           }

          Public function getPost(Request $request){
               $allPosts =post::all();
               return view("view",compact('allPosts'));
               


               
            } 
        
}
