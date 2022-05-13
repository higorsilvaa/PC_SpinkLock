from matplotlib.pyplot import *
import numpy as np

tempos = np.loadtxt(sys.argv[1],dtype=np.float64,delimiter=",")

x = tempos[:,0]
l = ["TASLock","TTASLock","BackoffLock","Semaphore","ALock","CLHLock","MCSLock","CompositeLock"]
c = ["blue","red","black","purple","green","gray","orange","pink"]
for i in range(1,np.shape(tempos)[1]):
	y = tempos[:,i]
	plot(x, y, "--", color=c[i-1], label=l[i-1])
	plot(x, y, ".", color=c[i-1])


ylabel("Tempo(s)");
xlabel("Threads")
maior = np.max(tempos[:,1:])
ylim((0.0,maior+0.1*maior))
grid()
title(sys.argv[2])
legend()

savefig('../Results/SpinLocks.png', format='png', dpi=300)
