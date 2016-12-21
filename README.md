# FastScrollLayout
Android fast scroll layout from ListView

代码基于 [SuperSaiyanScrollView](https://github.com/nolanlawson/SuperSaiyanScrollView) 修改而来.

## 使用方式, ListView 放在 FastScrollLayout 中: 

xml : 

```
<com.nolanlawson.supersaiyan.widget.FastScrollLayout
    android:id="@+id/scroll"
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ListView
        android:id="@android:id/list"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbars="none"/>
</com.nolanlawson.supersaiyan.widget.FastScrollLayout>

```
往 ListView中填充数据后滚动即可看到效果.


## 定制 scroll thumb 
将自己要替换的scroll thumb 放在drawable目录中, 资源名字为 scroll_thumb、scroll_thumb_pressed, 分别表示一般情况下的图片以及按下后的图片。

