# Changelog

All notable changes to the Tencent Cloud KMS Stress Tester project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Comprehensive code comments throughout all Java source files
  - Added JavaDoc-style documentation to Main.java
  - Added class and method documentation to MetadataCredentialClient.java
  - Added class and field documentation to TemporaryCredential.java
- Detailed comments in shell scripts
  - Enhanced stress-kms.sh with parameter descriptions
  - Improved setup-environment.sh with better user feedback
- Comprehensive README.md overhaul
  - Added detailed table of contents
  - Expanded prerequisites section with role permissions
  - Added installation verification steps
  - Included usage examples for various scenarios
  - Added "Understanding Results" section with sample output
  - Added "Advanced Configuration" section
  - Added "Troubleshooting" section with common issues
  - Added performance guidelines and recommendations
- Created CHANGELOG.md to track project changes

### Changed
- README.md structure significantly improved for better readability
- Updated setup-environment.sh with emoji indicators for better UX
- Enhanced script error handling and user feedback

### Fixed
- Corrected README.md formatting issues
- Fixed inconsistent documentation about required permissions
- Clarified CMK_ID parameter behavior (auto-creation vs. existing key)

## [1.0.0] - Initial Release

### Added
- Core stress testing functionality
  - Concurrent KMS encryption/decryption operations
  - Configurable worker threads (concurrency parameter)
  - Configurable test duration
- Mixed operation support
  - Configurable encryption/decryption ratio
  - Automatic ciphertext queue management for decryption operations
- Automatic key management
  - Temporary CMK creation when no key is specified
  - Automatic key cleanup (disable and schedule deletion)
  - Support for using existing CMKs
- CVM role authentication
  - Integration with CVM metadata service
  - Automatic temporary credential retrieval
  - Support for custom role names
- Real-time statistics reporting
  - Periodic stats updates (every 5 seconds)
  - Live RPS (requests per second) tracking
  - Separate tracking for encrypt/decrypt operations
  - Error count monitoring
  - Active thread tracking
- Performance metrics
  - Peak RPS tracking (overall, encrypt, decrypt)
  - Average latency calculation
  - Success rate percentage
  - Operation ratio verification (config vs. actual)
- Comprehensive final report
  - Total operations summary
  - Success/error breakdown
  - Per-operation RPS metrics
  - Peak performance indicators
  - Average latency statistics
- Configuration flexibility
  - System property-based configuration
  - Environment variable support
  - Configurable regions and endpoints
  - Internal endpoint support for lower latency
- Project infrastructure
  - Maven build configuration (pom.xml)
  - Assembly plugin for fat JAR creation
  - SLF4J logging framework
  - Jackson JSON parsing
  - Tencent Cloud SDK integration
- Shell scripts
  - stress-kms.sh - Main test runner script
  - setup-environment.sh - Environment verification and build script
- Documentation
  - Initial README.md with basic usage
  - Example results in results/example-results.md
  - Apache 2.0 License

### Technical Details
- Java 11+ compatibility
- Thread-safe atomic counters for statistics
- Custom AtomicDouble implementation for decimal metrics
- Fixed-size thread pool with configurable concurrency
- Graceful shutdown with timeout handling
- HTTP client timeouts optimized for metadata service
- Base64 encoding/decoding for KMS plaintext
- Concurrent ciphertext queue with size limits (max 1000)

---

## Version History

- **[Unreleased]** - Documentation improvements and code comments
- **[1.0.0]** - Initial functional release with core features

---

## Notes

### Breaking Changes
None in current version.

### Deprecated Features
None in current version.

### Security
- Credentials are never logged or displayed in plaintext
- Temporary keys are automatically cleaned up after testing
- Uses Tencent Cloud internal endpoints when available

### Known Issues
- None reported

### Future Enhancements
Planned for future releases:
- Support for additional KMS operations (GenerateDataKey, etc.)
- CSV/JSON export of test results
- Grafana dashboard integration
- Multi-region testing support
- Batch operation testing
- Custom payload size configuration
- Historical performance comparison
