# TellingMinutes
Time is precious, every minute counts~<br/>
<a href='https://raw.githubusercontent.com/FrannyZhao/TellingMinutes/master/res/telling-minutes.apk'>Download telling-minutes.apk</a>
<br/>
<hr>
<p><strong>Why:</strong></p>
早上要按时打卡上班,但经常因为不小心刷牙多刷了1分钟,或者蹲坑多蹲了2分钟而错过打卡最后一分钟。:cry: <br/>
早上的每一分钟都很重要啊,如果有一个钟能一直保持每分钟报时就好了。:stuck_out_tongue: <br/>
搜了一下应用商店似乎没有能满足我需求的应用,那么自己写一个吧~  :grinning: <br/>
In order to get to the office right on time and sleep a bit more in the morning, <br/>I can't waste even one minute in morning rush... :cry: <br/>
I always hope there is a voice keep telling me every minute, <br/>so that I won't waste time during brush teeth or change clothes... :stuck_out_tongue: <br/>
But I can't find such application online, so I code one for me~ :grinning: <br/>
<hr>
<p><strong>What:</strong></p>
只有一个界面,一打开就会开始每分钟自动报时：<br/>
Just one main interface, once the app is opened, it will begin to tell time every minute：<br/>
<img src='https://raw.githubusercontent.com/FrannyZhao/TellingMinutes/master/res/mainLayout.png' width="300px" style='border: #f1f1f1 solid 1px'/>
<br/>
应用启动后会在通知栏占一条位置,确保即使应用不可见的时候,也可以在后台保持报时. <br/>
There will be a notification once the application starts, in order to keep it alive even it's invisible.  <br/>
<img src='https://raw.githubusercontent.com/FrannyZhao/TellingMinutes/master/res/notification.png' width="300px" style='border: #f1f1f1 solid 1px'/>
<br/>
按返回键就可以退出应用啦, 通知栏也会移除这条通知的。<br/>
Press back key to quit the application, and the notification will be remove as well. <br/>
<hr>
<p><strong>Todo:</strong></p>
1. 支持英文报时,目前只能用中文报时......<br/>
Support telling minutes in English, currently only can speak in Chinese... <br/>
2. 要在设置里面打开谷歌tts才能报时, 待研究如何能自动设置tts... <br/>
Need to enable google tts manually... <br/>

等卡办好后,注册google cloud,使用翻译接口和上传接口...
