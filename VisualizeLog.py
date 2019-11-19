import csv
import numpy as np
import matplotlib.pyplot as plt 


with open('/Users/akhil/achilles/workspace/multitenantcache/logfile.log') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    
    output = []
    for line in csv_reader:
        if(line[1] == 'CLIENT_CACHE_STATE'):
            output.append(line[2:-1])
    output = np.array(output)
    output = np.transpose(output)
    state = np.split(output, 3)
    for x in state: 
        plt.plot(x[0], label = "line 1") 
        plt.plot(x[1], label = "line 2") 
        plt.plot(x[2], label = "line 3") 
    
    plt.xlabel('x - axis') 
    plt.ylabel('y - axis') 
    plt.title('Two lines on same graph!') 
    
    plt.legend() 
    plt.show() 

import matplotlib.pyplot as plt
import numpy as np

t = np.arange(0.01, 5.0, 0.01)
s1 = np.sin(2 * np.pi * t)
s2 = np.exp(-t)
s3 = np.sin(4 * np.pi * t)

ax1 = plt.subplot(311)
plt.plot(t, s1)
plt.setp(ax1.get_xticklabels(), fontsize=6)

# share x only
ax2 = plt.subplot(312, sharex=ax1)
plt.plot(t, s2)
# make these tick labels invisible
plt.setp(ax2.get_xticklabels(), visible=False)

# share x and y
ax3 = plt.subplot(313, sharex=ax1, sharey=ax1)
plt.plot(t, s3)
plt.xlim(0.01, 5.0)
plt.show()
  
# x1 = [1,2,3] 
# y1 = [2,4,1] 
# plt.plot(x1, y1, label = "line 1") 
  
# x2 = [1,2,3] 
# y2 = [4,1,3] 
# plt.plot(x2, y2, label = "line 2") 
  
