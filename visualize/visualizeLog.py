import csv
import numpy as np
import matplotlib.pyplot as plt 

def readLogFile():
    with open('log.log') as csv_file:
        csv_reader = csv.reader(csv_file, delimiter=',')
        
        output = []
        timestamp = []
        for line in csv_reader:
            if(line[1] == 'CLIENT_CACHE_STATE'):
                output.append(line[2:-1])
                timestamp.append(int(line[0]))
        output = np.array(output)
        output = np.int_(output)
        output = np.transpose(output)
        state = np.split(output, 3)
        startTime = timestamp[0]
        for i in range(len(timestamp)):
            timestamp[i] = timestamp[i] - startTime
        return state, np.array(timestamp)

cacheState, timestamps = readLogFile()

activeList = []
for x in cacheState: 
    activeList.append(x[1])

print(timestamps)
print(activeList)

fig, ax = plt.subplots()
ax.stackplot(timestamps, activeList[0], activeList[1],activeList[2])
plt.show()
