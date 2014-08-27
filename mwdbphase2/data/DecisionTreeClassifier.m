function DecisionTreeClassifier(trainingFile, testingFile, labelsFile, outputFile)
training = csvread(trainingFile);
labels   = csvread(labelsFile);
testing  = csvread(testingFile);
tree = ClassificationTree.fit(training,labels)
prediction = predict(tree, testing)
csvwrite(outputFile, prediction)
