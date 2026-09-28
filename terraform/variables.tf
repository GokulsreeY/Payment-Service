variable "aws_region" {
  description = "AWS region used for the payments infrastructure"
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "Name of the EKS cluster"
  type        = string
  default     = "payments-cluster"
}