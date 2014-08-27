function final_lsh(k,l,outfile,gestureCount)
load ('ggSVD.csv','-ASCII');
load ('combinedQueryGesture.csv','-ASCII');
T1=lsh('e2lsh',k,l,size(ggSVD,1),ggSVD);
[nnlsh,numcand]=lshlookup(combinedQueryGesture(:,1),ggSVD,T1,'k',gestureCount);
nnlsh
csvwrite(outfile,nnlsh);