function final_lsh(gg,query,gg1,query1,k,l,outfile)
load (gg,'-ASCII');
load (query,'-ASCII');
T1=lsh('e2lsh',k,l,size(gg1,1),gg1);
[nnlsh,numcand]=lshlookup(query1(:,1),gg1,T1);
csvwrite(outfile,nnlsh);