# Contributing to Healthcare ML Prediction Service

Thank you for considering contributing to the Healthcare ML Prediction Service! We welcome contributions from the community to help improve and expand the functionality of our service.

## Code of Conduct

This project and everyone participating in it are governed by the [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to [contact@example.com].

## How to Contribute

1. Fork the repository
2. Create a new branch (`git checkout -b feature/YourFeature`)
3. Make your changes
4. Commit your changes (`git commit -am 'Add some feature'`)
5. Push to the branch (`git push origin feature/YourFeature`)
6. Create a new Pull Request

## Development Setup

1. Clone the repository
2. Navigate to the `backend/ml/prediction-service` directory
3. Install dependencies

```bash
pip install -r requirements.txt
```

4. Run the service

```bash
python src/main.py
```

## Testing

We use pytest for testing. To run the tests:

```bash
pytest
```

## Documentation

We use Sphinx for documentation. To build the documentation:

```bash
cd docs
make html
```

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
