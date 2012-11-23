Part of the code is adapted from Qtjambi example file.

Technical Specification:
1. Use QtJambi 4.5 as framework
2. Export runnable jar file use Fatjar plugin to extract related jar packge and generate build script( xml file)
3. Use Jsmooth to convert jar pack to exe file
4. Use zip tool to compress mp3 files and the executable file
5. the programe is portable, user can even record their own mp3 file and rename as "wn.mp3" etc.



White noise – 白噪声的妙用- FROM 小众软件

题记：
作者早上再刷微博的时候，居然听见室友的闹钟不停的响~ di di di di 烦了半小时！！
不想理会室友~~让他继续睡，我们做正经的事情 :D
于是写了400行代码，这个噪音消除器就出炉了。。
==================================================================================================
Q:什么是白噪声？

类似电视机收音机没信号时，发出的那个沙沙声。维基的解释：
白噪声(White noise)，是一种功率谱密度为常数的随机信号或随机过程。即，此信号在各个频段上的功率是一样的。

Q:白噪声有什么用？

当你需要专心工作，而周遭总是有繁杂的声音时，就可以选用这两种声音来加以遮蔽。一般来说，通常的情况下你可以选用白色噪音，而粉红色噪音则是特别针对说话声的遮蔽材料。
粉红色噪音又被称做频率反比 (1/f) 噪音，因为它的能量分布与频率成反比，或者说是每一个八度音程 (Octave) 能量就衰退 3 dB。
它可以帮助睡眠、增强隐私、防止分心、掩饰耳鸣、缓解偏头痛、配置音响设备…等，用途相当广。

==================================================================================================
本软件提供3种最常见，最有效的噪声。
如果需要满足自己需求，可以去 whitenoisemp3s.com 和 www.simplynoise.com 录制自己喜欢的声音.
取名为wn.mp3 （或rn.mp3, bn.mp3）即可。软件每次自动载入这3个文件。
作者就是从simplenoise 网站使用Audacity自己录的哦！！

欢迎提交bug
想看源代码的请微博私信好了。^--^

联系方式：
新浪微博：白羊座不务正业

或者猛击：
weibo.com/dailyjava

