function KNNClassifier(sampleFile, trainingFile, classFile, k , distance, rule, outputFile)

sample    = csvread(sampleFile);
training  = csvread(trainingFile);
classes   = csvread(classFile);

output    = knnclassify(sample, training, classes, k, distance,rule)
csvwrite(outputFile,output)