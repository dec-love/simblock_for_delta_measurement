## Source code 1

## What is SimBlock?

SimBlock is an open source blockchain network simulator, developed by Distributed Systems Group, Tokyo Institute of Technology & Kyoto University.

We used SimBlock to measure the propagation time between each validator and determined the worst propagation time. The results are output to simulator/src/dist/output/input.txt. Please refer to the following User Guide for instructions on how to use SimBlock.

https://dsg-titech.github.io/simblock/

- [User Guide (English)](https://github.com/dsg-titech/simblock/blob/master/docs/en/usage.md)
- [User Guide (Japanese)](https://github.com/dsg-titech/simblock/blob/master/docs/jp/usage.md)

We set the obtained worst propagation time in the modified SimBlock and run simulations to verify the validity of the theoretical stale block rate. Please refer to the following URL for the modified SimBlock.
(modfied SimBlock URL: [https://github.com/dec-love/modfied_simblock](https://github.com/dec-love/modfied_simblock))

## License

SimBlock is licensed under the Apache License, Version2.0.
