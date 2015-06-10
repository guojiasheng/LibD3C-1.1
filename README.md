# LibD3C-1.1
Ensemble classifiers with a clustering and dynamic selection strategy and solve imbalance peoblem

LibD3C-1.1 is the upgrades for the software LibD3C.LibD3C is an ensemble classifiers with a clustering and dynamic selection strategy.For the LibD3C-1.1 ,it could solve imbalance data.

usage:
 
 Usage is the same no mater windows or Linux
 
(1)download the LibD3C-1.0.jar and classifier.xml(This file includes all base classifiers for LibD3C-1.0,you can even add other base classifier by yourself)

(2)CrossValidation
 2.1.balance data
    Java -jar LibD3C-1.0 -C fold-num trainFile
    
 2.2.imbalance data    
    Java -jar LibD3C-1.0 -m -C fold-num trainFile
    
fold-num is the crossValiditon folds.
trainFile is the file path for the training data,softWare can only identify .arff file

(3)prediction
 3.1.balance data
    Java -jar LibD3C-1.0 -p trainFile predictFile resultFile
    
 3.2.imbalance data    
    Java -jar LibD3C-1.0 -m -p trainFile predictFile resultFile
    
trainFile is the file path for the training data,softWare can only identify .arff file
predictFile is the file path for prediction file
resultFile is the file path save prediction result

  
