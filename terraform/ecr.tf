resource "aws_ecr_repository" "payment_service" {
  name                 = "payment-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "payment-service"
  }
}

output "ecr_repository_url" {
  value = aws_ecr_repository.payment_service.repository_url
}