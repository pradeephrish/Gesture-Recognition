function [alpha,beta] = ldamain(inputpath,outputpath,k,emmax,demmax)
% wrapper of Latent Dirichlet Allocation, standard model.
% [alpha,beta] = ldamain(inputpath,k,[emmax,demmax])
% $Id: ldamain.m,v 1.1 2004/11/08 12:41:58 dmochiha Exp $
% d      : data of documents
% k      : # of classes to assume
% emmax  : # of maximum VB-EM iteration (default 100)
% demmax : # of maximum VB-EM iteration for a document (default 20)
if nargin < 5
  demmax = 20;
  if nargin < 4
    emmax = 100;
  end
end
%inputpath=transpose(inputpath);
d = fmatrix(inputpath);
[alpha,beta] = lda(d,k,emmax,demmax);
beta=transpose(beta);
csvwrite(outputpath,beta);