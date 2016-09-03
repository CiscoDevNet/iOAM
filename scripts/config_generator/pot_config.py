# Program to generate proof of transit (POT) configuration.
# the program takes a minimum of two inputs, number of services
# and maximum number of bits. The third optional input specifies
# the config format which can be IOS or VPP.

import sys
import random
from functools import reduce

# fast calculation of prime
from pot_config_util import *

# function to multiply all the elements of the list by a given number
def mul_list_int(list,n):
    return [n*x for x in list]

# function to subtract all the elements of the list by a given number
def sub_from_list_int(list,n):
    return [n-x for x in list]

#function to evaluate a given polynomial while modding it with a given number
def eval_modded_poly(coeffs,x,mod):
    pow_x=x
    res = 0
    for coeff in coeffs:
        res+=coeff*pow_x
        res=res%mod
        pow_x*=x
    return res

# main function
def main(argv):

    if(len(argv)<2):
        sys.exit('Usage: pot_config.py <number_of_services> <max_num_of_bits> <(optional)config_for(VPP/IOS)>')
    if(not(argv[0].isdigit() and argv[1].isdigit())):
        sys.exit('Usage: pot_config.py <number_of_services> <max_num_of_bits> <(optional)config_for(VPP/IOS)>')
    if(len(argv)==3 and (argv[2].upper()!= "IOS" and argv[2].upper()!= "VPP")):
        sys.exit('Usage: pot_config.py <number_of_services> <max_num_of_bits> <(optional)config_for(VPP/IOS)>')
    if(len(argv)>3):
        sys.exit('Usage: pot_config.py <number_of_services> <max_num_of_bits> <(optional)config_for(VPP/IOS)>')
    if(int(argv[0])<2):
        sys.exit('number of services in a chain cannot be less that two')
    num_of_services = int(argv[0])
    num_of_bits = int(argv[1])
    max_num = (2**num_of_bits)
    
    coeff_poly_1 = []
    coeff_poly_2 = []
    prime_valid = False

    itr = 0

    # run the loop until a valid prime is found
    while (not prime_valid):
        itr+=1
        # if it is the 10th iteration, probably the number of bits specified are
        # not enough for the operation.
        if(itr>10):
            sys.exit('increase the number of bits or cross your fingers and try again')
        for i in range(num_of_services):
            coeff_poly_1.append(random.randint(1,max_num))
            coeff_poly_2.append(random.randint(1,max_num))

        prime = next_prime(max(coeff_poly_1+coeff_poly_2))
        if (prime<max_num):
            prime_valid=True

    secret_1 = coeff_poly_1[0]
    coeff_poly_1 = coeff_poly_1[1:]
    coeff_poly_2 = coeff_poly_2[1:]
    service_indices = list(range(2,num_of_services*2+1,2))
    random.shuffle(service_indices)
    # polynomial(index) + secret
    secret_share_poly_1=[eval_modded_poly(coeff_poly_1,index,prime)+secret_1 for index in service_indices]
    secret_share_poly_1 = [x%prime for x in secret_share_poly_1]
    # polynomial(index)
    public_poly_2 = [eval_modded_poly(coeff_poly_2,index,prime) for index in service_indices]
    # multiply -1*index with every other members of the list
    lpc_num = [mul_list_int(service_indices[:index]+service_indices[index+1:],-1) for index in range(num_of_services)]
    # multiply all the members of the list and mod them with prime
    lpc_num=[reduce(lambda x,y:x*y, elem)%prime for elem in lpc_num]
    # subtract index from every other members of the list
    lpc_den = [sub_from_list_int(service_indices[:index]+service_indices[index+1:],service_indices[index]) for index in range(num_of_services)]
    # multiply all the members of the list and mod them with prime
    lpc_den=[reduce(lambda x,y:x*y, elem)%prime for elem in lpc_den]
    lpc = [num*modinv(den,prime) for num,den in zip(lpc_num,lpc_den)]
    lpc = [x%prime for x in lpc]
    # print in the desired format
    if(len(argv)==2):
        print("Encap Node:")
        print("service index",service_indices[0])
        print("prime number",prime)
        print("secret_share",secret_share_poly_1[0])
        print("lpc",hex(lpc[0]))
        print("polynomial2 ", public_poly_2[0])
        print("bits-in-random",num_of_bits)
        print("")
        for i in range(1,num_of_services-1):
            print("Intermediate Node {}:".format(i))
            print("service index",service_indices[i])
            print("prime number",prime)
            print("secret_share",secret_share_poly_1[i])
            print("lpc",hex(lpc[i]))
            print("polynomial2 ", public_poly_2[i])
            print("bits-in-random",num_of_bits)
            print("")
        print("Dencap/Verifier Node:")
        print("service index",service_indices[-1])
        print("prime number",prime)
        print("verifier key",secret_1)
        print("secret_share",secret_share_poly_1[-1])
        print("lpc",hex(lpc[-1]))
        print("polynomial2 ", public_poly_2[-1])
        print("bits-in-random",num_of_bits)
    elif(argv[2].upper()=="IOS"):
        print("Encap Node:")
        print("ipv6 ioam service-chaining example")
        print("service-chain insert")
        print("prime number {}".format(hex(prime)))
        print("secret key {}".format(hex(secret_share_poly_1[0])))
        print("lpc {}".format(hex(lpc[0])))
        print("polynomial2 {}".format(hex(public_poly_2[0])))
        print("bits-in-random {}".format(num_of_bits))
        print("")
        for i in range(1,num_of_services-1):
            print("Intermediate Node {}:".format(i))
            print("ipv6 ioam service-chaining example")
            print("prime number {}".format(hex(prime)))
            print("secret key {}".format(hex(secret_share_poly_1[i])))
            print("lpc {}".format(hex(lpc[i])))
            print("polynomial2 {}".format(hex(public_poly_2[i])))
            print("bits-in-random {}".format(num_of_bits))
            print("")
        print("Dencap/Verifier Node:")
        print("ipv6 ioam service-chaining example")
        print("service-chain analyze")
        print("prime number {}".format(hex(prime)))
        print("secret key {}".format(hex(secret_share_poly_1[-1])))
        print("verifier key {}".format(hex(secret_1)))
        print("lpc {}".format(hex(lpc[-1])))
        print("polynomial2 {}".format(hex(public_poly_2[-1])))
        print("bits-in-random {}".format(num_of_bits))
        print("")
    elif(argv[2].upper()=="VPP"):
        print("Encap Node:")
        print("set pot profile name example id 0 prime-number {} secret_share {} lpc {} polynomial2 {} bits-in-random {}"
              .format(hex(prime),hex(secret_share_poly_1[0]),hex(lpc[0]),hex(public_poly_2[0]),num_of_bits))
        print("")
        for i in range(1,num_of_services-1):
            print("Intermediate Node {}:".format(i))
            print("set pot profile name example id 0 prime-number {} secret_share {} lpc {} polynomial2 {} bits-in-random {}"
                  .format(hex(prime),hex(secret_share_poly_1[i]),hex(lpc[i]),hex(public_poly_2[i]),num_of_bits))
            print("")
        print("Dencap/Verifier Node:")
        print("set pot profile name example id 0 validate-key {} prime-number {} secret_share {} lpc {} polynomial2 {} bits-in-random {}"
                  .format(hex(secret_1),hex(prime),hex(secret_share_poly_1[-1]),hex(lpc[-1]),hex(public_poly_2[-1]),num_of_bits))
        
    
    

if __name__ == "__main__":
    main(sys.argv[1:])

