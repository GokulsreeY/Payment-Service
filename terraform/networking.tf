#vpc where all the resources will be deployed to
resource "aws_vpc" "payments" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "payments-vpc"
  }
}

#data to get the available availability zones
data "aws_availability_zones" "available" {
  state = "available"
}
#subnets group and isolate resources from certain ip addresses
#public subnet a and b will be allocated
# in two different availability zones and their own specific ip address range
#the public subnets will be exposed to the internet via an API gateway
resource "aws_subnet" "public_a" {
  vpc_id                  = aws_vpc.payments.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true

  tags = {
    Name                     = "payments-public-a"
    "kubernetes.io/role/elb" = "1" #helps kubernetes/aws load balancing understand this subnet is used for external
  }
}

resource "aws_subnet" "public_b" {
  vpc_id                  = aws_vpc.payments.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = data.aws_availability_zones.available.names[1]
  map_public_ip_on_launch = true

  tags = {
    Name                     = "payments-public-b"
    "kubernetes.io/role/elb" = "1" #helps kubernetes/aws load balancing understand this subnet is used for external
  }
}

#private subnet a and b will be allocated in two different availability zones
# and their own specific ip address range
#the private subnets will not be exposed, but will have a NAT gateway to allow outbound access
# (connecting to other aws services like ECR)
resource "aws_subnet" "private_a" {
  vpc_id            = aws_vpc.payments.id
  cidr_block        = "10.0.11.0/24"
  availability_zone = data.aws_availability_zones.available.names[0]

  tags = {
    Name                              = "payments-private-a"
    "kubernetes.io/role/internal-elb" = "1" #helps kubernetes/aws load balancing understand this subnet is used for internal
  }
}

resource "aws_subnet" "private_b" {
  vpc_id            = aws_vpc.payments.id
  cidr_block        = "10.0.12.0/24"
  availability_zone = data.aws_availability_zones.available.names[1]

  tags = {
    Name                              = "payments-private-b"
    "kubernetes.io/role/internal-elb" = "1" #helps kubernetes/aws load balancing understand this subnet is used for internal
  }
}
#gateway from the internet to the public subnets within the VPC
resource "aws_internet_gateway" "payments" {
  vpc_id = aws_vpc.payments.id

  tags = {
    Name = "payments-igw"
  }
}

#route table is what actually tells the public subnets to route to the API gateway
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.payments.id

  route {
    cidr_block = "0.0.0.0/0" #Traffic destined for another VPC address stays inside the VPC. Everything else can go toward the Internet Gateway.
    gateway_id = aws_internet_gateway.payments.id
  }

  tags = {
    Name = "payments-public-rt"
  }
}

#associate the route table with the public subnets
resource "aws_route_table_association" "public_a" {
  subnet_id      = aws_subnet.public_a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_b" {
  subnet_id      = aws_subnet.public_b.id
  route_table_id = aws_route_table.public.id
}

#public elastic ip as it will represent the private resources when communicating outward
resource "aws_eip" "nat" {
  domain = "vpc"

  tags = {
    Name = "payments-nat-eip"
  }
}
#Nat gateway for private resources to have outbound access
resource "aws_nat_gateway" "payments" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public_a.id

  depends_on = [aws_internet_gateway.payments]

  tags = {
    Name = "payments-nat"
  }
}
#route table for nat gateway
resource "aws_route_table" "private" {
  vpc_id = aws_vpc.payments.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.payments.id
  }

  tags = {
    Name = "payments-private-rt"
  }
}
#nat gateway and route table association 
resource "aws_route_table_association" "private_a" {
  subnet_id      = aws_subnet.private_a.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "private_b" {
  subnet_id      = aws_subnet.private_b.id
  route_table_id = aws_route_table.private.id
}